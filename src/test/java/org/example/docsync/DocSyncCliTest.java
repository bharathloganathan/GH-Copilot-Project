package org.example.docsync;

import java.nio.file.Files;
import java.nio.file.Path;

public class DocSyncCliTest {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(".").toAbsolutePath();
        DocSyncCli.sync(root.toString());
        DocSyncCli.verify(root.toString());
        assert Files.exists(root.resolve("docs/ui/student-ui.md"));
        assert Files.exists(root.resolve("docs/api/endpoints.md"));
        System.out.println("DocSyncCliTest passed");
    }
}
