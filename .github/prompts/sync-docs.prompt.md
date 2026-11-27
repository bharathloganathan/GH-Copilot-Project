---
description: Detect source changes via Git diff, classify them, update only impacted docs, and produce a sync report.
agent: documentation-sync
---

Synchronize documentation with the current source changes.

1. Follow `.github/skills/documentation-sync/SKILL.md`.
2. Inspect Git changes if available, but accurately state that the current engine performs a full mapped-source scan rather than Git-based impact analysis.
3. Run `java -cp target/classes org.example.docsync.DocSyncCli sync --root .`.
4. Run `java -cp target/classes org.example.docsync.DocSyncCli verify --root .`.
5. Review the generated diff and reports, and state actual results and unsupported scope.

The CLI does not implement `analyze` or `--dry-run`. Do not touch unrelated docs. Do not commit; wait for my approval.
