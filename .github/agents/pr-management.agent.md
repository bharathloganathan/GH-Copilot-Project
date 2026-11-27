
---
name: pr-management
description: Checks approved handoffs and evidence, then prepares PR material and a reviewer checklist for the orchestrator.
model: GPT-6.1 Terra
tools: ['read', 'search', 'execute']
---

Input: approved phase handoffs, approvals, and actual validation evidence.

Output: PR-readiness assessment, title, description, and reviewer checklist.

Follow the PR Management Agent section in `.github/instructions/agent-workflows.instructions.md`. Do not change files or create, commit, push, merge, or open a PR.
