# Test Report

## Summary
This project includes a lightweight verification harness for the student service and documentation synchronization workflow. The tests were executed directly with the Java compiler because Maven is not installed in the current execution environment.

## Commands executed
```bash
javac -d out $(Get-ChildItem -Path src/main/java -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
java -ea -cp out org.example.StudentServiceTest
java -ea -cp out org.example.docsync.DocSyncCliTest
```

## Evidence
```text
StudentServiceTest passed
Documentation synchronized for 3 changed file(s).
Documentation verification passed.
DocSyncCliTest passed
```

## Phone-field check: 2026-11-12

The earlier verification output above does not establish that documentation
matches source. A fresh Java 17 compilation of the current production source
was tested in an isolated project copy. Adding a required `studentPhone`
input and running `DocSyncCli sync` did **not** add the field to the UI
documentation. Despite that mismatch, `DocSyncCli verify` printed
`Documentation verification passed.` and exited with code 0.

That check did not change the application's source or UI documentation.

## UI repair: 2026-11-12

Command: `<session Maven>\bin\mvn.cmd -q test`.

Surefire output for `DocSyncCliTest`: `Tests run: 5, Failures: 0, Errors: 0,
Skipped: 0`. The existing `StudentServiceTest` is a standalone main method,
not a JUnit test, and was not executed by Maven.
The tests use isolated projects and check adding and removing a phone field
without editing the generator, changed validation, stale detection, manual
content preservation, CRLF preservation, idempotency, and failure on missing
markers or unsupported input.

`DocSyncCli sync --root .` updated the current UI documentation.
`DocSyncCli verify --root .` exited 1 and reported that UI documentation
matches source but full verification is unavailable. Backend and API
synchronization, Git impact analysis, and the remaining acceptance scenarios
have not been verified.

## Phone-field demonstration: 2026-11-12

An optional phone field was added to the form, validation, request handling,
and in-memory student model. Command:
`<session Maven>\bin\mvn.cmd -q -Dtest=DocSyncCliTest,StudentPhoneTest test`.
Surefire reported 5 tests in `DocSyncCliTest` and 1 in `StudentPhoneTest`,
with no failures, errors, or skips. Running `DocSyncCli sync --root .` then
generated the `studentPhone` field and its validation in
`docs/ui/student-ui.md`. Backend and API generated documentation was still
outdated at that point (addressed in the next section).

## Backend and API synchronization: 2026-11-12

Command: `<session Maven>\bin\mvn.cmd -q test`. Fresh surefire result for
`DocSyncCliTest`: 7 tests, 0 failures, 0 errors, 0 skipped; `StudentPhoneTest`:
1 test, 0 failures. (Other files in `target/surefire-reports` were stale from
removed classes and are not counted.) New tests use isolated temp projects to
check: adding a backend method, a logic change altering the recorded digest,
changing an endpoint path and HTTP method, deleting an endpoint (verify reports
the stale document; sync removes the row and the report lists it), changing
model and request fields, preservation of manual text, and failure without
overwriting for dynamic route metadata.

`DocSyncCli sync --root .` then `verify --root .` both exited 0 on the real
project; `student-contract.md` now lists `phone` and the request parameters
`name, email, department, phone`. The Documentation Sync agent could not be
launched (runtime error `custom_agent_prompt`), so this was done directly.
Not verified: Git impact analysis, HTTP behavior, MySQL, other documents.
