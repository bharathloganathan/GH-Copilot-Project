---
description: Start the complete Agentic SDLC for US-001 (documentation synchronization).
agent: orchestrator
---

Start the full Agentic SDLC for **US-001: Automated Documentation Synchronization**.

1. Repository discovery (frontend, backend, API, existing docs).
2. Delegate to `requirements` → `docs/requirements.md`; stop for my approval.
3. Delegate to `architecture` → `docs/architecture.md`, `docs/design-review.md`; stop for approval.
4. Delegate to `planning` → `docs/impl-plan.md`; stop for approval.
5. Delegate implementation to `development`, then doc updates to `documentation-sync`.
6. Delegate verification to `quality-review` (tests + doc consistency); return failures for correction.
7. Delegate release prep to `pr-management`; stop for approval.

Never commit, push or merge without my explicit approval. Show phase-wise progress.
