---
applyTo: "docs/**/*.md,README.md"
---

# Documentation Rules

## Naming conventions
- File names are lowercase kebab-case: `student-service.md`, `student-contract.md`, `endpoints.md`.
- Folders: `docs/ui/` (frontend), `docs/backend/` (one file per Java class), `docs/api/` (endpoints and contracts), `docs/reports/` (generated reports).
- Top-level SDLC documents keep fixed names: `requirements.md`, `architecture.md`, `design-review.md`, `impl-plan.md`, `code-review.md`, `test-report.md`, `changelog.md`.

## Source-code traceability
- Every generated block declares its sources: `<!-- docsync:begin <block-id> sources=<path>[,<path>] -->`.
- Source paths are repo-relative with forward slashes. Each generated section also shows a `Source:` line.
- Requirement IDs (`US-001`, `AC-n`) are referenced in SDLC documents to maintain requirement traceability.

## Documentation synchronization
- Only content between `docsync:begin` and `docsync:end` markers is machine-managed; edit it only through `DocSyncCli sync`.
- Never edit, reformat, or delete text outside markers. Manual notes belong in the `## Manual notes` section.
- Update only documents mapped to changed sources. Removed sources are flagged `OUTDATED`, not deleted (deletion needs human approval).
- Never document behavior that is not present in source.

## Markdown formatting
- One H1 per file; headings increase one level at a time.
- Tables for fields, endpoints, methods; fenced code blocks with a language tag.
- LF line endings, no trailing whitespace, single blank line between blocks.

## Validation
- After synchronizing, run `DocSyncCli verify`; it must report no MISSING, STALE or OUTDATED items before approval.
- A block is STALE when it differs from what the source currently produces.
- Record validation results in `docs/reports/verify-report.md` and `docs/test-report.md`.
