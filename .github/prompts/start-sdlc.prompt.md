---
description: Start the complete Agentic SDLC for US-001 (documentation synchronization).
agent: orchestrator
---

Start the full Agentic SDLC for **US-001: Automated Documentation Synchronization**.

1. Inspect the worktree and repository; do not assume earlier work or approvals are current.
2. Delegate requirements, architecture and planning in order. Present a phase summary and stop for my approval at each gate; specialists return structured handoffs and do not create separate phase Markdown files.
3. After the approved plan, delegate implementation to `development`, then generated documentation updates to `documentation-sync`.
4. Delegate testing and review to `quality-review`; return defects to the owning specialist and repeat validation as needed.
5. Delegate release readiness and PR content to `pr-management`.
6. Only after all phases and approvals are complete, write one consolidated `docs/sdlc-report.md` with requirements, design, plan, implementation, docs sync, test/review evidence, limitations, approvals, and PR title/description/checklist.

Generated product documentation under `docs/ui/`, `docs/backend/` and `docs/api/` remains managed by DocSync. Its sync/verify reports under `docs/reports/` are also retained.

Never commit, push or merge without my explicit approval. Show phase-wise progress. Follow `.github/instructions/agent-workflows.instructions.md`.
