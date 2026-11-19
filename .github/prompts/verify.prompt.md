---
description: Verify docs accurately reflect the current source; report missing, outdated and inconsistent docs.
agent: quality-review
---

Verify documentation against source.

1. Run `java -cp target/classes org.example.docsync.DocSyncCli verify --root .`.
2. Use the `documentation-validation` skill to inspect each MISSING / STALE / OUTDATED item.
3. Report results in a table (doc, block, status, source). Write `docs/reports/verify-report.md` (the CLI does this).
4. Do not fix anything; recommend running `/sync-docs` for stale items and ask before deleting outdated docs.
