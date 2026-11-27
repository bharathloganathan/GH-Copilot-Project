---
applyTo: "docs/**/*.md,README.md"
---

# Documentation Rules

## Naming conventions
- File names are lowercase kebab-case: `student-service.md`, `student-contract.md`, `endpoints.md`.
- Folders: `docs/ui/` (frontend), `docs/backend/` (one file per Java class), `docs/api/` (endpoints and contracts), `docs/reports/` (generated reports).
- The final agent-authored end-to-end SDLC report is `docs/sdlc-report.md`. Agent phases provide structured handoffs; do not create one Markdown file per agent or phase. Existing `requirements.md`, `architecture.md`, `design-review.md`, `impl-plan.md`, `code-review.md`, `test-report.md`, `changelog.md`, and `pr-description.md` are existing reference material, not outputs for new workflow runs unless requested.
- DocSync-generated product documentation in `docs/ui/`, `docs/backend/`, `docs/api/`, plus engine reports in `docs/reports/`, are separate functional outputs and must continue to be generated/updated as required.

## Source-code traceability
- Every generated block declares its sources: `<!-- docsync:begin <block-id> sources=<path>[,<path>] -->`.
- Source paths are repo-relative with forward slashes. Each generated section also shows a `Source:` line.
- The supplied user-story ID and acceptance-criteria IDs are referenced in the final `docs/sdlc-report.md` to maintain requirement traceability.

## Documentation synchronization
- Only content between `docsync:begin` and `docsync:end` markers is machine-managed; edit it only through `DocSyncCli sync`.
- Never edit, reformat, or delete text outside markers. Manual notes belong in the `## Manual notes` section.
- The current DocSync CLI scans all configured mapped sources during `sync`; do not claim it selectively updates only Git-impacted documents. Report changed-source impact separately from the CLI's actual scan. Removed sources are not automatically deleted; ask for human approval before removing their documentation.
- Never document behavior that is not present in source.

## Markdown formatting
- One H1 per file; headings increase one level at a time.
- Tables for fields, endpoints, methods; fenced code blocks with a language tag.
- LF line endings, no trailing whitespace, single blank line between blocks.

## Validation
- After synchronizing, run the supported `DocSyncCli verify` and report its actual result. Do not claim it classifies generic MISSING or OUTDATED sources unless that behavior is implemented.
- A generated block is stale when it differs from what the source currently produces.
- The CLI writes validation results to `docs/reports/verify-report.md`. Return executed test results to the orchestrator for the final `docs/sdlc-report.md`.
