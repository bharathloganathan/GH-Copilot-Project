# Agentic SDLC Workflow — Document-Driven Feature Delivery

This document describes the agent workflow configuration. It is not a generated
run report. For each run, the **orchestrator** uses the user-story document
supplied by the human as the scope source, coordinates specialist agents, and
waits for explicit human approval after every phase.

All eight custom agents request GPT-6.1 Terra in their frontmatter. Actual model
selection depends on whether the Copilot host and account expose that model.
Detailed role procedures are in `.github/instructions/agent-workflows.instructions.md`;
that file is the canonical source for role behavior, phase order and approval
gates. This document is a human-readable overview; the agent files themselves stay concise. This project has one consolidated skill,
`.github/skills/documentation-sync/SKILL.md`.

## Agents

| Agent | Role | Deliverable(s) |
|---|---|---|
| `orchestrator` | SDLC coordinator; delegates, tracks phases and approvals | Final `docs/sdlc-report.md` |
| `requirements` | Analyst for the supplied user story | Structured requirements handoff |
| `architecture` | Solution architect and design reviewer | Structured architecture/review handoff |
| `planning` | Implementation planner | Structured task-plan handoff |
| `development` | Core Java developer (DocSync engine + demo app) | Source code and tests |
| `quality-review` | QA: review, tests and doc/source validation | Structured findings and test-evidence handoff |
| `documentation-sync` | Doc synchronization specialist | Generated product docs and CLI sync/verify reports |
| `pr-management` | Release/PR manager | Structured PR/reviewer-checklist handoff |

## Workflow phases

```mermaid
flowchart TD
    A[Supplied user story] --> B[1. Requirements]
    B -->|human approval| C[2. Architecture]
    C -->|human approval| D[3. Planning]
    D -->|human approval| E[4. Development]
    E -->|human approval| F[5. Documentation Sync]
    F -->|human approval| G[6. Quality Review]
    G -->|issues found: fix, resync if source changed, revalidate| E
    G -->|human approval| H[7. PR Management]
    H -->|human approval| I[8. Final SDLC Report]
    I -->|human approval| J[Commit / Merge: separately authorized]
```

### Phase 1 — Requirements
- **Agent:** `requirements`
- **Input:** supplied user-story document and stakeholder notes
- **Handoff:** functional/non-functional requirements, assumptions, dependencies,
  measurable acceptance criteria and traceability. No phase Markdown file.
- **Gate:** 🧑 Human approves requirements before architecture begins.

### Phase 2 — Architecture
- **Agent:** `architecture`
- **Input:** approved requirements handoff
- **Handoff:** target design, component responsibilities, data flow, risks,
  mitigations and review findings. State clearly which elements are implemented
  versus planned. No phase Markdown file.
- **Gate:** 🧑 Human approves architecture before planning.

### Phase 3 — Planning
- **Agent:** `planning`
- **Input:** approved requirements + architecture handoffs
- **Handoff:** dependency-ordered tasks with scope, owners, AC traceability and
  observable completion criteria. No phase Markdown file.
- **Gate:** 🧑 Human approves the plan before implementation.

### Phase 4 — Development
- **Agent:** `development`
- **Input:** approved implementation-plan handoff
- **Output:** DocSync engine (`org.example.docsync`) and demo app changes;
  code follows project standards (small classes, `PreparedStatement` when SQL
  exists, literal `new Route(...)` declarations and `validationRules`)
- **Rule:** never edits another agent's deliverables.
- **Gate:** 🧑 Human approves the implementation evidence before documentation synchronization.

### Phase 5 — Documentation Sync
- **Agent:** `documentation-sync`
- **Input:** human-approved implementation and changed-file handoff
- **Current pipeline:** full scan of mapped source files → generated-block
  update → validation (`verify`) → sync/verify reports. Git-based impact
  analysis, baseline fallback and selective engine updates are not implemented.
- **Mapped feature facts:** UI form controls and Registered Students table
  headers; frontend validation; backend service methods/digest; declared API
  routes and create parameters; Student model fields; and the source-derived
  successful `GET /students` response contract.
- **Rules:** only generated blocks (`<!-- docsync:begin -->` …
  `<!-- docsync:end -->`) are rewritten; manual content is preserved; every
  generated fact traces to source. The GET response mapping does not verify live
  HTTP dispatch or runtime behavior. The engine currently handles four mapped docs.
- **Gate:** 🧑 Human approves the generated diff and actual sync/verify results.

### Phase 6 — Quality Review
- **Agent:** `quality-review`
- **Input:** implementation and documentation-sync evidence
- **Handoff:** actionable findings and exact JUnit 5/build/verification evidence
  (no claimed-but-unrun tests). No standalone review/test Markdown file.
- **Loop:** defects go back to the owner; source changes require another DocSync
  run before final verification. Re-review after fixes.
- **Gate:** 🧑 Human approves quality findings and validation evidence.

### Phase 7 — PR Management
- **Agent:** `pr-management`
- **Input:** all approved phase handoffs and verified evidence
- **Handoff:** readiness checklist, README/changelog recommendations, PR title
  and description, limitations and reviewer checklist.
- **Gate:** 🧑 Human approves the PR-preparation handoff. This does not authorize PR creation, commit or merge.

### Phase 8 — Final SDLC Report
- **Owner:** `orchestrator`
- **Input:** approved handoffs and actual execution evidence from every phase
- **Output:** created or updated `docs/sdlc-report.md`, then presented for final
  human approval. No report is marked complete while a phase or approval is pending.

## Final workflow report

After phases 1–7 are complete and approved, the orchestrator creates or updates
the consolidated `docs/sdlc-report.md` and presents it for the phase-8 approval.
The workflow is complete only after that approval. The report contains the
requirements and AC trace, architecture and design review, plan/completion record, implementation summary,
documentation-sync changes and CLI evidence, test/review findings, limitations,
approval history, readiness status, and PR title/description/checklist. Do not
create a separate phase report for each agent.

The generated product documentation (`docs/ui/`, `docs/backend/`, `docs/api/`)
and DocSync CLI reports (`docs/reports/sync-report.md`,
`docs/reports/verify-report.md`) remain separate because they are functional
outputs of the product, not per-agent SDLC handoffs. Existing standalone phase
documents are retained as reference material and are not rewritten in new runs
unless the user requests it.

## Global rules

1. **Source code is the source of truth** — docs never drive code.
2. **Human approval gates:** after every phase, including review of the final
   SDLC report. Commit, push, merge, deploy and PR creation require separate,
   explicit authorization.
3. **Hand-off through the orchestrator:** specialists return structured phase
   results to the orchestrator; the final lifecycle record is consolidated in
   `docs/sdlc-report.md`.
4. **Ownership:** no agent edits another agent's deliverable.
5. **Security:** no secrets in source/docs; DB credentials via `DB_URL`,
   `DB_USER`, `DB_PASSWORD`; parameterized SQL; validated, bounded input.
6. **Evidence:** test results only count with execution evidence in
   `docs/sdlc-report.md` and the underlying test command output.