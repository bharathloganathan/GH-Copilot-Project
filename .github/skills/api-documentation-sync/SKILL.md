---
name: api-documentation-sync
description: Synchronize REST endpoint and request/response contract documentation from the Java HTTP server routes and model records.
---

# API Documentation Sync

## Purpose
Keep `docs/api/endpoints.md` and `docs/api/<type>-contract.md` equal to the route table and model records.

## Instructions
- Endpoints come from `route("METHOD", "/path", "summary", status, "Request", "Response", handler)` calls.
- Contracts come from `record` components; fields in `READ_ONLY_FIELDS` appear in responses only.
- A removed route disappears from the table and is listed under *Removed* in the report; changed method/URL appears as remove + add.

## Execution workflow
1. Detect changes in `api/` and `model/`. 2. Run `sync`. 3. Confirm added/removed/changed rows in the report.
4. `verify`; endpoint deletions must not remain in docs.
