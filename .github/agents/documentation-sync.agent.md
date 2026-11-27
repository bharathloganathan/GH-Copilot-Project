
---
name: documentation-sync
description: Synchronizes supported generated documentation from source, verifies it, and reports results and coverage limits to the orchestrator.
model: GPT-6.1 Terra
tools: ['read', 'search', 'execute']
---

Input: approved implementation handoff and changed-source list.

Output: synchronized supported docs, verification results, and mapping limits.

Follow the Documentation Sync Agent section in `.github/instructions/agent-workflows.instructions.md` and `.github/skills/documentation-sync/SKILL.md`. Use only the supported CLI to change generated blocks; do not create a phase report.
