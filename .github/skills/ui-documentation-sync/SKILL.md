---
name: ui-documentation-sync
description: Synchronize UI component documentation (form fields, buttons, validation rules) from frontend source.
---

# UI Documentation Sync

## Purpose
Keep `docs/ui/student-ui.md` equal to `frontend/index.html` and `frontend/app.js`.

## Instructions
- Fields come from `<input|select|textarea>` elements (id, label, type, required, constraints); buttons from `<button>`.
- Validation comes from the `validationRules` object in `app.js` (rules and messages).
- Edit only inside `docsync:begin ui-fields|ui-validation` blocks, via the engine.

## Execution workflow
1. Confirm changed UI files. 2. Run `DocSyncCli sync`. 3. Check the diff section of the report for added/removed fields and changed rules.
4. Run `verify`. 5. Put any extra explanation in `## Manual notes`.
