---
applyTo: "src/test/java/**/*.java"
---

# Testing Instructions

- Use JUnit 5, the project's existing Maven configuration, and the current test conventions.
- Prefer isolated temporary project trees for CLI tests; do not mutate repository source or docs as a test side effect.
- Cover both generated content and preservation of text outside generated markers.
- Include failure assertions for malformed markers, unsupported source syntax, stale documentation and concurrent changes where applicable.
- Run the narrowest relevant test selection after edits; run the full Maven test suite when practical.
- Return only executed commands and observed results to the orchestrator for inclusion in `docs/sdlc-report.md`. Do not count stale Surefire reports or standalone `main` methods as Maven test passes.
