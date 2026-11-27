# Copilot Instructions — Automated Documentation Synchronization

## Project purpose
This repository contains the Student Management demo and the higher-priority DocSync engine (`org.example.docsync`). The engine synchronizes selected UI, backend and API documentation from source for **US-001**. It does not currently implement every capability described in the original target architecture.

## Technology and actual implementation
- Frontend: plain HTML, CSS and JavaScript in `frontend/`.
- Backend: Core Java and `com.sun.net.httpserver` in `src/main/java/org/example/`.
- Build/tests: Maven and JUnit 5.
- The Student demo currently uses an in-memory repository. MySQL/JDBC persistence is not implemented.
- Do not add Spring Boot, microservices or unnecessary frameworks.

## Repository map
| Path | Purpose |
|---|---|
| `frontend/` | Student form, validation and styles |
| `src/main/java/org/example/api/` | HTTP server routes and handlers |
| `src/main/java/org/example/model/` | Student model |
| `src/main/java/org/example/repository/` | Demo persistence abstraction |
| `src/main/java/org/example/service/` | Demo business rules |
| `src/main/java/org/example/docsync/` | Documentation sync and verification CLI |
| `src/test/java/` | JUnit 5 tests |
| `docs/` | Requirements, design, generated docs, reports and release material |
| `.github/agents/` | Eight concise specialist agent definitions |
| `.github/instructions/` | Project-wide, path-scoped and agent-role guidance |
| `.github/skills/documentation-sync/` | The single consolidated documentation sync skill |

## Agent workflow outputs
The eight agents hand off structured findings to the orchestrator; they do not create one Markdown file per phase. After the complete requirements-to-PR workflow and approval gates, the orchestrator creates the single consolidated `docs/sdlc-report.md` containing the full lifecycle, traceability, evidence, limitations, approvals and PR material. Existing standalone SDLC documents are reference material unless the user asks to migrate them. This consolidation does not replace DocSync's product docs in `docs/ui/`, `docs/backend/`, `docs/api/` or its required sync/verify reports.

## Project instructions
Follow the applicable scoped instructions:
- `java.instructions.md` for Java production source.
- `frontend.instructions.md` for frontend source.
- `docsync.instructions.md` for the synchronization engine.
- `testing.instructions.md` for tests.
- `documentation.instructions.md` for Markdown docs.
- `agent-workflows.instructions.md` for detailed responsibilities of each custom agent.

## Current DocSync capabilities
The CLI supports only:
```text
java -cp target/classes org.example.docsync.DocSyncCli sync --root .
java -cp target/classes org.example.docsync.DocSyncCli verify --root .
```
Current mappings cover `frontend/index.html` form controls and registered-students table headers; `frontend/app.js` validation rules; `StudentService.java` public methods and digest; `StudentApiServer.java` route declarations, create parameters and the source-derived `GET /students` response contract; and `Student.java` record components. They write to four mapped docs under `docs/ui/`, `docs/backend/` and `docs/api/`.

The GET response contract is extracted from the Java source chain; it does not verify live HTTP dispatch or runtime behavior. The current engine does **not** implement Git-based impact analysis, an `analyze` command, `--dry-run`, a baseline fallback, general Java class documentation, or full HTTP dispatch/response verification. Do not claim these capabilities unless they are implemented and tested.

## Source and documentation rules
- Source is authoritative; never invent behavior based on plans or docs.
- Generated text is limited to matching `<!-- docsync:begin ... -->` / `<!-- docsync:end ... -->` blocks. Update it through the CLI; preserve everything outside the markers.
- Run `sync`, inspect its diff/report, then run `verify`. Report failures and limits accurately.
- Do not touch unrelated documentation. Do not delete docs for removed source without human approval.
- Do not say that custom agents or tests ran unless execution evidence exists.

## Agent and approval rules
- Keep each `.agent.md` concise; put detailed agent responsibilities in `agent-workflows.instructions.md`.
- Each agent requests `GPT-6.1 Terra`. The Copilot host/account must offer that model; otherwise the host may require selecting an available model.
- The orchestrator delegates distinct outputs and stops at human approval gates. Specialist agents do not edit another specialist's deliverables.
- Never commit, push, merge, deploy, approve, or perform destructive actions without the user's explicit authorization.

## Security and quality
- Never put credentials, tokens or personal secrets in source or docs.
- Validate and bound request input; return generic client-facing errors; use parameterized SQL if SQL is introduced.
- Keep static file serving constrained to intended files.
- Add focused JUnit 5 tests for behavior and failure conditions. Return only actually executed test results to the orchestrator for inclusion in the final `docs/sdlc-report.md`.
