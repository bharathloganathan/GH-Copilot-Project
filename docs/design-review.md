# Design Review

## Summary
The proposed architecture uses a deterministic pipeline: detect → classify → impact → sync → verify → report → approval. This keeps the engine simple and robust without requiring heavy frameworks.

## Findings
- Finding 1: The design relies on Git diff for authoritative detection; this is appropriate and avoids hook-only assumptions.
- Finding 2: The sync engine is restricted to generated blocks, which preserves manual notes and prevents accidental overwrites.
- Finding 3: A fallback baseline is necessary because Git may not be available in all environments.

## Decisions
- Keep the engine in Core Java with a CLI entry point `org.example.docsync.DocSyncCli`.
- Keep the data model small and explicit to minimize maintenance overhead.
- Require an explicit approval gate before commit/merge.
