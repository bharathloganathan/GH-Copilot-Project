# Copilot Instructions — Automated Documentation Synchronization

## Project overview
A simple **Student Management System** (demo app) plus a **DocSync engine** (`org.example.docsync`) that keeps
technical documentation in `docs/` synchronized with source code. The engine is the priority; the demo app only
exists to be changed and documented. Story: **US-001 Automated Documentation Synchronization**.

## Technology stack
- Frontend: HTML, CSS, JavaScript (`frontend/`)
- Backend: Core Java, Java HTTP Server (`com.sun.net.httpserver`), JDBC + MySQL
- Build: Maven (`pom.xml`). Version control: Git/GitHub
- **No** Spring Boot, microservices or extra frameworks. Test with JUnit 5 only.

## Layout
| Path | Purpose |
|---|---|
| `frontend/` | UI source (`index.html`, `app.js`, `style.css`) |
| `src/main/java/org/example/{model,repository,service,api,util}` | Demo backend |
| `src/main/java/org/example/docsync` | Documentation synchronization engine |
| `docs/` | requirements, architecture, design-review, impl-plan, `ui/`, `backend/`, `api/`, `reports/`, code-review, test-report, changelog |
| `.github/` | agents, instructions, prompts, skills, hooks |

## Coding standards
- Java: small classes, explicit types for public APIs, no unused code, `PreparedStatement` for all SQL.
- Keep the demo app extractable by the engine: API routes use `route("METHOD", "/path", "summary", status, "RequestType", "ResponseType", handler)`;
  UI validation lives in the `validationRules` object in `frontend/app.js`; read-only model fields are declared in `READ_ONLY_FIELDS`.
- Comment only code that needs clarification.

## Architecture rules
- Pipeline: change detection → classification → impact analysis → doc update → validation → report → human approval.
- Source code is the implementation source of truth. Docs never drive code.
- Generated doc content lives only inside `<!-- docsync:begin ... -->` / `<!-- docsync:end ... -->` blocks; everything outside is manual and must be preserved.

## Agent collaboration rules
- The **orchestrator** coordinates; specialist agents own distinct outputs (see `.github/agents/`). No agent edits another agent's deliverable.
- Hand off through the documents in `docs/`; record each phase and approval in the orchestrator's progress notes.
- Human approval is required after requirements, architecture, plan, and before any commit/merge.

## Documentation synchronization principles
1. Detect changes with Git diff (or the engine's baseline fallback) — never assume hooks see every edit.
2. Update only documentation impacted by changed sources; never touch unrelated docs.
3. Never invent functionality; every documented fact traces to a source file.
4. Validate docs against source after every sync (`verify`), and report failures.
5. Produce `docs/reports/sync-report.md` for every sync.

## Security requirements
- No secrets in source or docs; DB credentials come from environment variables (`DB_URL`, `DB_USER`, `DB_PASSWORD`).
- Validate and bound all request input; return generic errors; parameterized SQL only; prevent path traversal when serving static files.
- Never commit, push, merge, deploy, or run destructive commands without explicit human authorization (enforced in part by `pre-tool-use.ps1`).
- Do not claim tests passed without execution evidence recorded in `docs/test-report.md`.
