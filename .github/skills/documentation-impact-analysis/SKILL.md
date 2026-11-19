---
name: documentation-impact-analysis
description: Map modified source files to affected documentation and classify changes as UI, BACKEND or API. Use after change detection.
---

# Documentation Impact Analysis

## Purpose
Update only the docs that are affected.

## Instructions
| Source | Category | Documentation (block) |
|---|---|---|
| `frontend/index.html` | UI | `docs/ui/student-ui.md` (`ui-fields`) |
| `frontend/app.js` | UI | `docs/ui/student-ui.md` (`ui-validation`) |
| `src/main/java/**/api/*.java` | API | `docs/api/endpoints.md` (`api-endpoints`) |
| `src/main/java/**/model/*.java` (records) | API + BACKEND | `docs/api/<type>-contract.md` (`api-contract`), `docs/backend/<type>.md` |
| other `src/main/java/**/*.java` | BACKEND | `docs/backend/<class>.md` (`backend-class`) |

## Execution workflow
1. Take the change list. 2. Look up every changed path in the table (engine: `DocPlan`). 3. Unmapped changes → warn, do not guess.
4. Deleted sources → their docs become OUTDATED (flag; do not delete). 5. Report impacted docs only.
