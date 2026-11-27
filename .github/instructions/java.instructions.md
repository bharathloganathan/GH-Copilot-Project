---
applyTo: "src/main/java/**/*.java"
---

# Java Instructions

- Use the project's configured Java release and Core Java APIs; do not add Spring, microservice frameworks or dependencies without an approved requirement.
- Keep classes focused and public APIs explicitly typed. Follow existing package and naming conventions.
- Use `PreparedStatement` for all SQL and environment variables for database credentials if database access is implemented.
- Validate and bound external input, return generic client-facing errors, and avoid logging secrets.
- For the Student demo, keep route declarations in `StudentApiServer.routes()` as literal `new Route(...)` entries when they must be extracted by the current engine.
- Preserve existing behavior unless a task explicitly calls for a behavior change. Add focused tests for changes.
- Do not put generated documentation content or secrets in Java source.
