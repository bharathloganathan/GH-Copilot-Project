# PR: US-001 Automated Documentation Synchronization

## Summary
Adds a DocSync engine (`org.example.docsync`) that regenerates UI, backend and API documentation blocks from source. It also adds a Student Management demo with an optional `phone` field to exercise the sync. Text outside `<!-- docsync:begin/end -->` blocks is preserved.

## Changed files
- Engine: `DocSyncCli`, `UiSync`, `BackendApiSync`, `GeneratedBlock`, `JavaSourceExtractor` (`src/main/java/org/example/docsync/`)
- Demo app: `Main`, `StudentApiServer`, `Student`, `StudentRepository`, `StudentService`, `frontend/*`
- Tests: `DocSyncCliTest`, `StudentPhoneTest` (plus other docsync tests)
- Docs: `docs/ui`, `docs/backend`, `docs/api`, `docs/reports`, `docs/test-report.md`, `docs/code-review.md`, `README.md`
- Agents and instructions: `.github/agents/*` (8 agents), `.github/instructions/documentation.instructions.md`

## Test evidence
- `mvn -q test` was run and passed: `DocSyncCliTest` (7 tests) and `StudentPhoneTest` (1 test), 0 failures.
- `DocSyncCli sync --root .` then `verify --root .` exited 0 on the real project.
- Manual check: removing `phone` removed it from the generated docs, and restoring it brought it back. Details are in `docs/test-report.md`.
- The app was served on `http://localhost:8000`. That check was manual.

## Known limitations
- Git-diff impact analysis is not integrated, so sync rescans the mapped files.
- Route entries are declarations from `routes()`, not proof of HTTP dispatch. Only literal `new Route(...)` arguments are supported.
- Business-logic meaning is not extracted. A source digest flags logic changes for human review.
- The demo uses an in-memory repository (no MySQL). It returns `Record.toString()` with a JSON content type.
- `StudentServiceTest` is a plain `main`, not a JUnit test.
- The custom agents (including `documentation-sync`) could not be launched in this runtime (`custom_agent_prompt` error). Sync and verify were run through the CLI directly.
- No security review has been completed.

## Reviewer checklist
- [ ] `mvn test` passes locally
- [ ] `DocSyncCli sync --root .` then `verify --root .` exits 0
- [ ] Adding or removing a field in source changes the generated docs
- [ ] Manual notes outside generated blocks are unchanged
- [ ] No secrets in the diff (DB credentials come from `DB_URL`, `DB_USER`, `DB_PASSWORD`)
- [ ] Limitations above are acceptable
- [ ] Human approval given before merge
