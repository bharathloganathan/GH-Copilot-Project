---
applyTo: "src/main/java/org/example/docsync/**/*.java"
---

# DocSync Engine Instructions

- Source code is authoritative. Extract only facts that the implementation can reliably parse; fail with a useful error rather than inventing output.
- Current commands are `sync` and `verify`. Do not document or invoke Git `analyze`, baseline fallback, dry-run, automatic classification or deletion handling as implemented unless those capabilities are added and tested.
- Keep source-to-document mappings explicit and repository-relative. Current mappings cover UI form controls and registered-students table headers, frontend validation rules, `StudentService` public methods/digest, `StudentApiServer` route declarations/create parameters and the source-derived `GET /students` response contract, and `Student` record components. The GET contract extractor traces the source chain; it does not verify live HTTP behavior.
- Replace only the matching `docsync:begin/end` block. Reject missing, duplicate, nested or malformed markers and generated content containing markers.
- Preserve text outside generated blocks and existing line-ending style. Detect concurrent document edits before writing.
- Keep `sync` idempotent and make `verify` fail with a non-zero exit status for stale generated content.
- Add JUnit 5 tests for extraction, idempotency, preservation, stale detection and fail-safe behavior before expanding the supported syntax.
