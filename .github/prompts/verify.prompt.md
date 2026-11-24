---
description: Verify docs accurately reflect the current source; report missing, outdated and inconsistent docs.
agent: quality-review
---

Verify documentation against source.

1. Run `java -cp target/classes org.example.docsync.DocSyncCli verify --root .`.
2. Follow the verification section in `.github/skills/documentation-sync/SKILL.md`.
3. Report exact mapped docs and blocks that are stale, plus the CLI's actual supported scope. The current verifier does not classify generic MISSING or OUTDATED sources.
4. The CLI writes `docs/reports/verify-report.md`. Do not claim it checked manual prose, HTTP dispatch or unmapped docs.
5. Do not fix anything; recommend running `/sync-docs` for stale generated blocks and ask before deleting docs.
