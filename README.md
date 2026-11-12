# Student Management Documentation Sync Demo

This project demonstrates an automated documentation synchronization workflow for a simple Student Management System written in Java. The application includes a minimal HTML/CSS/JS front end and a Java HTTP server backend. The documentation engine focuses on source-driven updates to UI, backend, and API docs.

## Key directories
- `frontend/` — UI files
- `src/main/java/` — backend and docsync engine
- `docs/` — generated and manual SDLC documentation
- `.github/` — custom agents, prompts, instructions, skills, and hooks

## Build and run
Use the Java compiler directly if Maven is unavailable in the environment:

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out org.example.Main
```

## Documentation sync
```bash
java -cp out org.example.docsync.DocSyncCli sync .
java -cp out org.example.docsync.DocSyncCli verify .
```
