package org.example.docsync;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class JavaSourceExtractor {
    record Route(String method, String path, String summary, String requestType, String responseType) {
    }

    record Component(String name, String type) {
    }

    record JavaType(String name, String kind, List<String> methods, List<Component> components) {
        JavaType {
            methods = List.copyOf(methods);
            components = List.copyOf(components);
        }
    }

    record Result(List<JavaType> types, List<Route> routes, List<String> requestParameters, String sourceDigest) {
        Result {
            types = List.copyOf(types);
            routes = List.copyOf(routes);
            requestParameters = List.copyOf(requestParameters);
        }
    }

    private JavaSourceExtractor() {
    }

    static Result extract(String source) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IOException("Java source extraction requires a JDK, not a standalone JRE");
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///Source.java"),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        try (var manager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            JavacTask task = (JavacTask) compiler.getTask(null, manager, diagnostics,
                    List.of("-proc:none"), null, List.of(file));
            Iterable<? extends CompilationUnitTree> units = task.parse();
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                    throw new IOException("Invalid Java at line " + diagnostic.getLineNumber()
                            + ": " + diagnostic.getMessage(null));
                }
            }
            List<JavaType> types = new ArrayList<>();
            List<Route> routes = new ArrayList<>();
            Set<String> parameters = new LinkedHashSet<>();
            for (CompilationUnitTree unit : units) {
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void unused) {
                        List<String> methods = new ArrayList<>();
                        List<Component> components = new ArrayList<>();
                        for (Tree member : node.getMembers()) {
                            if (member instanceof MethodTree method
                                    && method.getModifiers().getFlags().contains(Modifier.PUBLIC)
                                    && method.getReturnType() != null) {
                                methods.add(method.getModifiers() + method.getReturnType().toString()
                                        + " " + method.getName() + "("
                                        + String.join(", ", method.getParameters().stream()
                                                .map(Object::toString).toList()) + ")"
                                        + (method.getThrows().isEmpty() ? "" : " throws "
                                                + String.join(", ", method.getThrows().stream()
                                                        .map(Object::toString).toList())));
                            } else if (node.getKind() == Tree.Kind.RECORD
                                    && member instanceof VariableTree variable
                                    && !variable.getModifiers().getFlags().contains(Modifier.STATIC)) {
                                components.add(new Component(variable.getName().toString(),
                                        variable.getType().toString()));
                            }
                        }
                        types.add(new JavaType(node.getSimpleName().toString(),
                                node.getKind().name(), methods, components));
                        return super.visitClass(node, unused);
                    }

                    @Override
                    public Void visitNewClass(NewClassTree node, Void unused) {
                        if (enclosingMethod("routes") && node.getIdentifier().toString().equals("Route")) {
                            if (node.getArguments().size() != 6) {
                                throw new IllegalArgumentException("Unsupported Route constructor");
                            }
                            List<String> values = new ArrayList<>();
                            for (int i = 0; i < 5; i++) {
                                values.add(stringLiteral(node.getArguments().get(i), "Route metadata"));
                            }
                            routes.add(new Route(values.get(0), values.get(1), values.get(2),
                                    values.get(3), values.get(4)));
                        }
                        return super.visitNewClass(node, unused);
                    }

                    @Override
                    public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
                        if (enclosingMethod("createStudent")
                                && node.getMethodSelect() instanceof MemberSelectTree select
                                && select.getExpression().toString().equals("params")) {
                            if (!select.getIdentifier().contentEquals("getOrDefault")
                                    || node.getArguments().size() != 2) {
                                throw new IllegalArgumentException("Unsupported request parameter access: " + node);
                            }
                            parameters.add(stringLiteral(node.getArguments().get(0), "Request parameter name"));
                        }
                        return super.visitMethodInvocation(node, unused);
                    }

                    private boolean enclosingMethod(String name) {
                        var path = getCurrentPath();
                        while (path != null) {
                            if (path.getLeaf() instanceof MethodTree method) {
                                return method.getName().contentEquals(name);
                            }
                            path = path.getParentPath();
                        }
                        return false;
                    }
                }.scan(unit, null);
            }
            return new Result(types, routes, new ArrayList<>(parameters), digest(source));
        } catch (IllegalArgumentException e) {
            throw new IOException("Unsupported Java source: " + e.getMessage(), e);
        }
    }

    private static String stringLiteral(Tree tree, String label) {
        if (tree instanceof LiteralTree literal && literal.getValue() instanceof String value) {
            return value;
        }
        throw new IllegalArgumentException(label + " must be string literals");
    }

    private static String digest(String source) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(source.replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
