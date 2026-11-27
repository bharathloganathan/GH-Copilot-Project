---
description: Start the complete document-driven Agentic SDLC for the supplied user story.
agent: orchestrator
---

Use the supplied user-story document as the requirements source of truth; preserve its IDs and do not invent missing requirements. If the document or path is missing or unreadable, ask for it before starting.

Inspect the worktree and applicable instructions, then follow the canonical phase order, role responsibilities, handoff requirements and approval gates in `.github/instructions/agent-workflows.instructions.md`. Obtain my explicit approval after every phase before proceeding.

Run Documentation Sync after implementation and again after later source changes. Report the actual DocSync scope, results and limitations. The orchestrator creates or updates `docs/sdlc-report.md` from approved handoffs and execution evidence, then asks for final approval.

Show phase progress. Never commit, push, merge, deploy or open a PR without separate explicit authorization.
