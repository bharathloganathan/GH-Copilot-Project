---
name: backend-documentation-sync
description: Synchronize Java backend class documentation (types, public methods, source digest) from source.
---

# Backend Documentation Sync

## Purpose
Keep `docs/backend/<class>.md` equal to the Java source.

## Instructions
- Document type kind/name, record components, public method signatures.
- The `Source digest` (SHA-256 prefix of the normalized file) changes on any edit, so business-logic-only changes mark the doc for review.
- When the digest changed but signatures did not, add/refresh behavioral notes under `## Manual notes`, citing only visible source behavior.

## Execution workflow
1. Identify changed `.java` files. 2. Run `sync`. 3. Review report for added/removed methods.
4. For logic-only changes, read the diff and update manual notes. 5. `verify`.
