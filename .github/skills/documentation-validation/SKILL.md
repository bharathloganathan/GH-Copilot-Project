---
name: documentation-validation
description: Compare documentation with source code and identify missing, stale or outdated documentation.
---

# Documentation Validation

## Purpose
Prove docs match source.

## Instructions
- MISSING: expected doc/block absent. STALE: block differs from source-derived content. OUTDATED: block whose source no longer exists.
- Manual content outside blocks is never validated or changed.

## Execution workflow
1. `java -cp target/classes org.example.docsync.DocSyncCli verify --root .` (exit code 1 on problems).
2. Read `docs/reports/verify-report.md`. 3. For each problem name the doc, block, source, and fix (`sync`, or ask before deleting).
4. Re-run until clean; record the evidence in `docs/test-report.md`.
