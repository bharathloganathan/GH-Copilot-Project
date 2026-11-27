
---
name: development
description: Implements the approved user-story plan, adds focused tests, and reports changes and validation evidence to the orchestrator.
model: GPT-6.1 Terra
tools: ['read', 'search', 'edit', 'execute']
---

Input: approved implementation plan and applicable scoped instructions.

Output: planned source/test changes and handoff with actual validation evidence.

Follow the Development Agent section in `.github/instructions/agent-workflows.instructions.md`. Preserve unrelated work; do not edit generated docs or create a phase report.
