package org.example.docsync;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
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
import java.util.Map;
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

    record ListResponse(int status, String mediaType, List<String> fields) {
        ListResponse {
            fields = List.copyOf(fields);
        }
    }

    record Result(List<JavaType> types, List<Route> routes, List<String> requestParameters,
                  ListResponse listResponse, String sourceDigest) {
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
            List<MethodTree> apiMethods = new ArrayList<>();
            for (CompilationUnitTree unit : units) {
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void unused) {
                        if (node.getSimpleName().contentEquals("StudentApiServer")) {
                            for (Tree member : node.getMembers()) {
                                if (member instanceof MethodTree method
                                        && List.of("attachRoutes", "listStudents", "sendJson",
                                                "studentsAsJson", "appendJsonField",
                                                "appendJsonString").contains(
                                                        method.getName().toString())) {
                                    apiMethods.add(method);
                                }
                            }
                        }
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
            ListResponse listResponse = apiMethods.isEmpty() ? null : listResponse(apiMethods);
            return new Result(types, routes, new ArrayList<>(parameters), listResponse, digest(source));
        } catch (IllegalArgumentException e) {
            throw new IOException("Unsupported Java source: " + e.getMessage(), e);
        }
    }

    private static ListResponse listResponse(List<MethodTree> methods) throws IOException {
            MethodTree dispatch = uniqueMethod(methods, "attachRoutes");
            MethodTree list = uniqueMethod(methods, "listStudents");
            MethodTree send = uniqueMethod(methods, "sendJson");
            MethodTree encoder = uniqueMethod(methods, "studentsAsJson");
            MethodTree fieldWriter = uniqueMethod(methods, "appendJsonField");
            MethodTree stringWriter = uniqueMethod(methods, "appendJsonString");

            List<MethodInvocationTree> studentContexts = invocations(dispatch.getBody(), "createContext").stream()
                    .filter(call -> !call.getArguments().isEmpty()
                            && isString(call.getArguments().get(0), "/students"))
                    .toList();
            if (studentContexts.size() != 1 || studentContexts.get(0).getArguments().size() != 2
                    || !(studentContexts.get(0).getArguments().get(1) instanceof LambdaExpressionTree context)) {
                throw unsupported("expected one /students HTTP context");
            }
            List<SwitchTree> methodSwitches = new ArrayList<>();
            List<String> switchSelectors = new ArrayList<>();
            new com.sun.source.util.TreeScanner<Void, Void>() {
                @Override
                public Void visitSwitch(SwitchTree node, Void unused) {
                    switchSelectors.add(node.getExpression().toString());
                    if (compact(node.getExpression()).equals("exchange.getRequestMethod()")
                            || compact(node.getExpression()).equals("(exchange.getRequestMethod())")) {
                        methodSwitches.add(node);
                    }
                    return super.visitSwitch(node, unused);
                }
            }.scan(context.getBody(), null);
            if (methodSwitches.size() != 1) {
                throw unsupported("expected one HTTP-method dispatch switch, found selectors: "
                        + switchSelectors);
            }
            List<? extends CaseTree> getCases = methodSwitches.get(0).getCases().stream()
                    .filter(item -> item.getExpressions().stream()
                            .anyMatch(expression -> isString(expression, "GET")))
                    .toList();
            if (getCases.size() != 1 || getCases.get(0).getBody() == null
                    || !compact(getCases.get(0).getBody()).equals("listStudents(exchange);")) {
                throw unsupported("GET dispatch must call listStudents(exchange)");
            }

            if (list.getBody() == null || list.getBody().getStatements().size() != 1
                    || !(list.getBody().getStatements().get(0) instanceof ExpressionStatementTree statement)
                    || !(statement.getExpression() instanceof MethodInvocationTree sendCall)
                    || !methodName(sendCall).equals("sendJson")
                    || sendCall.getArguments().size() != 3
                    || !sendCall.getArguments().get(0).toString().equals("exchange")
                    || !isSuccessfulStatus(sendCall.getArguments().get(1))
                    || !(sendCall.getArguments().get(2) instanceof MethodInvocationTree jsonCall)
                    || !methodName(jsonCall).equals("studentsAsJson")
                    || jsonCall.getArguments().size() != 1
                    || !(jsonCall.getArguments().get(0) instanceof MethodInvocationTree studentsCall)
                    || !methodName(studentsCall).equals("getStudents")
                    || !(studentsCall.getMethodSelect() instanceof MemberSelectTree studentsSelect)
                    || !studentsSelect.getExpression().toString().equals("SERVICE")
                    || !studentsCall.getArguments().isEmpty()) {
                throw unsupported("listStudents must send a 2xx status with studentsAsJson(SERVICE.getStudents())");
            }

            if (send.getBody() == null || send.getBody().getStatements().size() != 4) {
                throw unsupported("sendJson response structure is unsupported");
            }
            if (!compact(send.getBody().getStatements().get(0))
                    .equals("byte[]payload=body.getBytes(StandardCharsets.UTF_8)")
                    || send.getParameters().size() != 3
                    || !send.getParameters().get(1).getName().contentEquals("status")
                    || !send.getParameters().get(2).getName().contentEquals("body")) {
                throw unsupported("sendJson must encode its body and retain the supplied status");
            }
            String statusForward = "exchange.sendResponseHeaders(status, payload.length);";
            Tree headerStatement = send.getBody().getStatements().get(1);
            if (!(headerStatement instanceof ExpressionStatementTree headerExpression)
                    || !(headerExpression.getExpression() instanceof MethodInvocationTree)) {
                throw unsupported("sendJson must set a literal Content-Type response header");
            }
            MethodInvocationTree headerCall = (MethodInvocationTree) headerExpression.getExpression();
            if (!methodName(headerCall).equals("add")
                    || !(headerCall.getMethodSelect() instanceof MemberSelectTree headerSelect)
                    || !headerSelect.getExpression().toString().equals("exchange.getResponseHeaders()")
                    || headerCall.getArguments().size() != 2
                    || !isString(headerCall.getArguments().get(0), "Content-Type")
                    || !(headerCall.getArguments().get(1) instanceof LiteralTree mediaLiteral)
                    || !compact(send.getBody().getStatements().get(2))
                            .equals(statusForward.replaceAll("\\s+", ""))
                    || !send.getBody().getStatements().get(3).toString().contains("os.write(payload)")) {
                throw unsupported("sendJson must set a literal Content-Type and forward its status and payload");
            }
            Object mediaValue = ((LiteralTree) headerCall.getArguments().get(1)).getValue();
            if (!(mediaValue instanceof String mediaType) || mediaType.isBlank()) {
                throw unsupported("Content-Type must be a non-empty string literal");
            }
            int status = ((Number) ((LiteralTree) sendCall.getArguments().get(1)).getValue()).intValue();

            if (encoder.getBody() == null || encoder.getBody().getStatements().size() != 3
                    || !(encoder.getBody().getStatements().get(0) instanceof VariableTree jsonBuilder)
                    || !(jsonBuilder.getInitializer() instanceof NewClassTree builder)
                    || !builder.getIdentifier().toString().equals("StringBuilder")
                    || builder.getArguments().size() != 1
                    || !isString(builder.getArguments().get(0), "[")) {
                throw unsupported("studentsAsJson must build a top-level JSON array");
            }
            List<ForLoopTree> loops = new ArrayList<>();
            new com.sun.source.util.TreeScanner<Void, Void>() {
                @Override
                public Void visitForLoop(ForLoopTree node, Void unused) {
                    loops.add(node);
                    return super.visitForLoop(node, unused);
                }
            }.scan(encoder.getBody(), null);
            if (loops.size() != 1
                    || !(encoder.getBody().getStatements().get(2) instanceof ReturnTree returnTree)
                    || !compact(returnTree.getExpression()).equals("json.append(']').toString()")) {
                throw unsupported("studentsAsJson must close and return the top-level JSON array");
            }
            ForLoopTree loop = loops.get(0);
            if (loop.getInitializer().size() != 1
                    || !compact(loop.getInitializer().get(0)).equals("intindex=0")
                    || !compact(loop.getCondition()).equals("index<students.size()")
                    || loop.getUpdate().size() != 1
                    || !compact(loop.getUpdate().get(0)).equals("index++;")
                    || !compact(loop).contains("Studentstudent=students.get(index);")
                    || !compact(loop).contains("if(index>0){json.append(',');}")) {
                throw unsupported("studentsAsJson must iterate students in order and delimit array items");
            }
            List<MethodInvocationTree> fieldCalls = invocations(loop, "appendJsonField");
            List<String> fields = new ArrayList<>();
            for (MethodInvocationTree call : fieldCalls) {
                if (call.getArguments().size() != 3
                        || !call.getArguments().get(0).toString().equals("json")
                        || !(call.getArguments().get(1) instanceof LiteralTree literal)
                        || !(literal.getValue() instanceof String key)
                        || !(call.getArguments().get(2) instanceof MethodInvocationTree accessor)
                        || !accessor.getArguments().isEmpty()
                        || !(accessor.getMethodSelect() instanceof MemberSelectTree select)
                        || !select.getExpression().toString().equals("student")) {
                    throw unsupported("appendJsonField must serialize literal Student accessors");
                }
                fields.add(key);
            }
            if (fields.isEmpty() || new LinkedHashSet<>(fields).size() != fields.size()
                    || !compact(loop).contains("json.append('{')")
                    || !compact(loop).contains("json.append('}')")) {
                throw unsupported("studentsAsJson object fields or array loop are unsupported");
            }
            String fieldWriterBody = compact(fieldWriter.getBody());
            String expectedFieldWriter = "{appendJsonString(json,name);json.append(':');"
                    + "if(value==null){json.append(\"null\");}else{appendJsonString(json,value);}}";
            if (!fieldWriterBody.equals(expectedFieldWriter)) {
                throw unsupported("appendJsonField must serialize a JSON key, colon and string/null value");
            }
            validateJsonStringWriter(stringWriter);
            return new ListResponse(status, mediaType, fields);
        }

    private static void validateJsonStringWriter(MethodTree method) throws IOException {
        if (!method.getReturnType().toString().equals("void")
                || method.getParameters().size() != 2
                || !method.getParameters().get(0).getName().contentEquals("json")
                || !method.getParameters().get(0).getType().toString().equals("StringBuilder")
                || !method.getParameters().get(1).getName().contentEquals("value")
                || !method.getParameters().get(1).getType().toString().equals("String")
                || method.getBody() == null
                || method.getBody().getStatements().size() != 3) {
            throw unsupported("appendJsonString must accept a JSON builder and string value");
        }
        if (!isAppendChar(method.getBody().getStatements().get(0), '"')) {
            throw unsupported("appendJsonString must wrap its output in JSON quotes");
        }
        if (!(method.getBody().getStatements().get(1) instanceof ForLoopTree loop)) {
            throw unsupported("appendJsonString must scan its value in a loop");
        }
        if (!isAppendChar(method.getBody().getStatements().get(2), '"')) {
            throw unsupported("appendJsonString must close the JSON string");
        }
        if (loop.getInitializer().size() != 1
                || !compact(loop.getInitializer().get(0)).equals("intindex=0")
                || !compact(loop.getCondition()).equals("index<value.length()")
                || loop.getUpdate().size() != 1
                || !compact(loop.getUpdate().get(0)).equals("index++;")
                || !(loop.getStatement() instanceof BlockTree loopBody)
                || loopBody.getStatements().size() != 2
                || !(loopBody.getStatements().get(0) instanceof VariableTree character)
                || !character.getName().contentEquals("character")
                || !compact(character.getInitializer()).equals("value.charAt(index)")
                || !(loopBody.getStatements().get(1) instanceof SwitchTree escapeSwitch)
                || !compact(escapeSwitch.getExpression()).equals("(character)")) {
            throw unsupported("appendJsonString must quote and scan each value character");
        }

        Map<Character, String> escapes = Map.of(
                '"', "\\\"", '\\', "\\\\", '\b', "\\b", '\f', "\\f",
                '\n', "\\n", '\r', "\\r", '\t', "\\t");
        Set<Character> found = new LinkedHashSet<>();
        boolean foundDefault = false;
        for (CaseTree item : escapeSwitch.getCases()) {
            if (item.getExpressions().isEmpty()) {
                if (foundDefault || !(item.getBody() instanceof BlockTree defaultBody)
                        || defaultBody.getStatements().size() != 1
                        || !(defaultBody.getStatements().get(0) instanceof IfTree controlCheck)
                        || !compact(controlCheck.getCondition()).equals("(character<32)")
                        || !(controlCheck.getThenStatement() instanceof BlockTree controlBody)
                        || controlBody.getStatements().size() != 3
                        || !isAppendString(controlBody.getStatements().get(0), "\\u00")
                        || !compact(controlBody.getStatements().get(1))
                                .equals("json.append(Character.forDigit((character>>4)&15,16));")
                        || !compact(controlBody.getStatements().get(2))
                                .equals("json.append(Character.forDigit(character&15,16));")
                        || !(controlCheck.getElseStatement() instanceof BlockTree ordinaryBody)
                        || ordinaryBody.getStatements().size() != 1
                        || !(ordinaryBody.getStatements().get(0) instanceof ExpressionStatementTree ordinary)
                        || !(ordinary.getExpression() instanceof MethodInvocationTree appendCharacter)
                        || !isJsonAppend(appendCharacter)
                        || appendCharacter.getArguments().size() != 1
                        || !appendCharacter.getArguments().get(0).toString().equals("character")) {
                    throw unsupported("appendJsonString must escape control characters as valid JSON");
                }
                foundDefault = true;
                continue;
            }
            if (item.getExpressions().size() != 1
                    || !(item.getExpressions().get(0) instanceof LiteralTree literal)
                    || !(literal.getValue() instanceof Character key)
                    || !escapes.containsKey(key)
                    || !found.add(key)
                    || !(item.getBody() instanceof ExpressionStatementTree escaped)
                    || !isAppendString(escaped, escapes.get(key))) {
                throw unsupported("appendJsonString escape cases are unsupported");
            }
        }
        if (!foundDefault || !found.equals(escapes.keySet())) {
            throw unsupported("appendJsonString must escape quotes, backslashes and JSON control characters");
        }
    }

    private static boolean isAppendChar(Tree statement, char expected) {
        if (!(statement instanceof ExpressionStatementTree expression)
                || !(expression.getExpression() instanceof MethodInvocationTree invocation)
                || !isJsonAppend(invocation) || invocation.getArguments().size() != 1) {
            return false;
        }
        return isChar(invocation.getArguments().get(0), expected);
    }

    private static boolean isAppendString(Tree tree, String expected) {
        if (!(tree instanceof ExpressionStatementTree expression)
                || !(expression.getExpression() instanceof MethodInvocationTree invocation)
                || !isJsonAppend(invocation) || invocation.getArguments().size() != 1) {
            return false;
        }
        Tree argument = invocation.getArguments().get(0);
        return argument instanceof LiteralTree literal && expected.equals(literal.getValue());
    }

    private static boolean isJsonAppend(MethodInvocationTree invocation) {
        return methodName(invocation).equals("append")
                && invocation.getMethodSelect() instanceof MemberSelectTree select
                && select.getExpression().toString().equals("json");
    }

    private static boolean isChar(Tree tree, char expected) {
        return tree instanceof LiteralTree literal && literal.getValue() instanceof Character value
                && value == expected;
    }

    private static MethodTree uniqueMethod(List<MethodTree> methods, String name) throws IOException {
            List<MethodTree> matches = methods.stream()
                    .filter(method -> method.getName().contentEquals(name)).toList();
            if (matches.size() != 1 || matches.get(0).getBody() == null) {
                throw unsupported("expected one implemented " + name + " method");
            }
            return matches.get(0);
        }

    private static List<MethodInvocationTree> invocations(Tree tree, String name) {
            List<MethodInvocationTree> matches = new ArrayList<>();
            new com.sun.source.util.TreeScanner<Void, Void>() {
                @Override
                public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
                    if (methodName(node).equals(name)) {
                        matches.add(node);
                    }
                    return super.visitMethodInvocation(node, unused);
                }
            }.scan(tree, null);
            return matches;
        }

    private static String methodName(MethodInvocationTree invocation) {
            Tree select = invocation.getMethodSelect();
            return select instanceof MemberSelectTree member ? member.getIdentifier().toString() : select.toString();
        }

    private static boolean isString(Tree tree, String expected) {
            return tree instanceof LiteralTree literal && expected.equals(literal.getValue());
        }

    private static boolean isSuccessfulStatus(Tree tree) {
            return tree instanceof LiteralTree literal && literal.getValue() instanceof Number value
                    && value.intValue() >= 200 && value.intValue() < 300;
        }

    private static String compact(Tree tree) {
            return tree.toString().replaceAll("\\s+", "");
        }

    private static IOException unsupported(String message) {
            return new IOException("Unsupported GET /students response source: " + message);
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
