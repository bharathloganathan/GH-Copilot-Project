---
description: Detect source changes via Git diff, classify them, update only impacted docs, and produce a sync report.
agent: documentation-sync
---

Synchronize documentation with the current source changes.

1. Use the `git-change-analysis` skill to list changed files (Git diff; baseline fallback if Git is unavailable).
2. Use `documentation-impact-analysis` to classify each change as **UI**, **BACKEND** or **API** and map it to docs.
3. Run `java -cp target/classes org.example.docsync.DocSyncCli sync --root .` (use `--dry-run` first if unsure).
4. Apply `ui-`, `backend-`, `api-documentation-sync` skills for any needed manual-notes follow-up (outside generated blocks only).
5. Run `documentation-validation`, then show `docs/reports/sync-report.md`.

Do not touch unrelated docs. Do not commit; wait for my approval.
