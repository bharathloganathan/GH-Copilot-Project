# Architecture

## Goal
Provide a simple, source-driven documentation synchronization workflow for frontend, backend, and API modifications in a Java-based Student Management System.

## Components
- `GitDiffDetector` — identifies modified, added, removed, and renamed files.
- `ChangeClassifier` — assigns each changed file to `UI`, `BACKEND`, or `API` categories.
- `ImpactAnalyzer` — maps changed files to affected docs under `docs/ui`, `docs/backend`, and `docs/api`.
- `DocSyncCli` — orchestrates sync and verification operations.
- `ReportWriter` — writes `docs/reports/sync-report.md` and `docs/reports/verify-report.md`.
- `HumanApprovalGate` — blocks commit and merge actions until approval is explicitly granted.

## Flow
1. Detect source changes (Git diff or baseline fallback).
2. Classify changes.
3. Map each change to relevant docs.
4. Regenerate only impacted documentation blocks.
5. Run verification to check for MISSING/STALE/OUTDATED entries.
6. Produce a report and wait for approval.

## Risks and mitigations
- Risk: Git is unavailable in a local environment. Mitigation: `DocSyncCli` supports a baseline fallback.
- Risk: Unrelated docs are modified. Mitigation: only the mapped documentation blocks are rewritten.
- Risk: source changes are not visible to hooks. Mitigation: Git diff remains the authoritative change detector.
