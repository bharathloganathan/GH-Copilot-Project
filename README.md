# Student Management Documentation Sync Demo

This project contains a Student Management demo and a documentation
synchronization engine. UI, backend service, API route and API contract
documentation are generated from source inside `docsync` blocks. Git impact
analysis is not yet integrated, and the demo's MySQL persistence is not
implemented (in-memory repository).

## Key directories
- `frontend/` — UI files
- `src/main/java/` — backend and docsync engine
- `docs/` — generated and manual SDLC documentation
- `.github/` — custom agents, prompts, instructions, skills, and hooks

## Build and run
Use the Java compiler directly if Maven is unavailable in the environment:

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out org.example.Main
```

## Documentation sync

From the repository root, build with Maven and run:

```powershell
java -cp target\classes org.example.docsync.DocSyncCli sync --root .
java -cp target\classes org.example.docsync.DocSyncCli verify --root .
```

`sync` regenerates the generated blocks in `docs/ui/student-ui.md`,
`docs/backend/student-service.md`, `docs/api/endpoints.md` and
`docs/api/student-contract.md` from the HTML controls, `validationRules`,
`StudentService` public methods (plus a source digest so logic changes show up),
`routes()` declarations, the `Student` record and the create endpoint's form
parameters. Text outside blocks is preserved. It writes
`docs/reports/sync-report.md`. `verify` exits 1 and lists STALE documents when
a block differs from source, and writes `docs/reports/verify-report.md`.
Unsupported constructs (dynamic route metadata, non-literal parameter names,
template literals in validation rules) fail rather than being guessed. Route
declarations are not proof of HTTP dispatch, and business-logic meaning needs
human review. The CLI never commits or merges.
