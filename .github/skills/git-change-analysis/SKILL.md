---
name: git-change-analysis
description: Analyze Git diffs to identify changed source files (added, modified, deleted, renamed). Use when determining what changed before synchronizing documentation.
---

# Git Change Analysis

## Purpose
Reliable list of changed files. Hooks do not see every edit; Git diff is the source of truth.

## Instructions
- Tracked sources: `frontend/**` and `src/main/java/**` (excluding `org/example/docsync` and `Main.java`).
- Statuses: `A` added, `M` modified, `D` deleted, `R` renamed (old → new).
- Include untracked files as added.

## Execution workflow
1. `git diff --name-status -M HEAD` (or `base..head` if the user gives revisions); `git ls-files --others --exclude-standard`.
2. If Git is unavailable or there is no commit: `DocSyncCli analyze` compares against `docs/reports/baseline.properties` (SHA-256 per file).
3. Output a table: status, path, old path.
4. Never stage or commit.
