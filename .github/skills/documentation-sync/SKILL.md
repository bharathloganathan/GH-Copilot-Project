---
name: documentation-sync
description: Run the current source-to-documentation synchronization workflow for the Student Management demo.
---

# Documentation Synchronization

Use this single skill for change discovery, impact scoping, source extraction, generated-doc updates and verification.

## Current supported scope

| Source | Extracted facts | Generated document |
|---|---|---|
| `frontend/index.html` | Form control IDs, types and required state | `docs/ui/student-ui.md` (`ui-fields`) |
| `frontend/app.js` | `validationRules` object | `docs/ui/student-ui.md` (`ui-validation`) |
| `src/main/java/org/example/service/StudentService.java` | Public method signatures and source digest | `docs/backend/student-service.md` |
| `src/main/java/org/example/api/StudentApiServer.java` | Literal `new Route(...)` declarations and literal `params.getOrDefault(...)` parameter names | `docs/api/endpoints.md`, `docs/api/student-contract.md` |
| `src/main/java/org/example/model/Student.java` | Record component names and types | `docs/api/student-contract.md` |

The engine currently has only `sync` and `verify` commands. It does not implement `analyze`, `--dry-run`, Git-based impact analysis or baseline fallback. A Git diff can help the agent scope and explain changes, but `sync` itself scans all mapped sources. Do not describe the CLI as updating only Git-impacted docs.

## Workflow

1. Inspect `git status` and the relevant diff when Git is available. Include untracked source files in the analysis. If Git data is unavailable, state that and still run the mapped-source sync rather than guessing.
2. Map changed files using the table above. For unmapped files, report no generated-doc mapping rather than inventing one.
3. Check generated marker blocks and preserve manual text. Do not edit generated block content directly.
4. From the repository root, execute:

   ```text
   java -cp target/classes org.example.docsync.DocSyncCli sync --root .
   java -cp target/classes org.example.docsync.DocSyncCli verify --root .
   ```

   Use the platform's classpath separator if needed. Do not pass unsupported flags.
5. Inspect `docs/reports/sync-report.md`, `docs/reports/verify-report.md`, and the final diff. Confirm whether each command succeeded; do not infer success from pre-existing report files.
6. Report changed source paths, mapped docs, command results, unsupported constructs, and any manual review needed.

## Safe handling and limits

- The engine preserves text outside generated markers and fails on malformed/missing markers or unsupported extraction constructs. Keep this failure behavior.
- Route declarations do not establish that the HTTP server dispatches the same routes. The model record is not proof of actual wire-format JSON.
- A service digest signals source changes but does not explain business behavior; a human must inspect source before adding manual prose.
- Do not claim full API contract extraction: current request extraction is limited to literal parameter names found in the supported method.
- Do not delete documentation because a source was removed without human approval.
- If an extraction fails, report it and leave docs for human resolution; never fabricate successful output.
- Do not edit application source, commit, push, merge or deploy during a documentation-only sync.
