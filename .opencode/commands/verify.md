---
description: Run the full Maven verify and fix every failure
---

Run `./mvnw verify` from the repository root. It runs the tests, the Spotless check, JaCoCo reporting, and the Maven enforcer.

- Fix every failure: compile errors, failing tests, Spotless violations (apply formatting with `./mvnw spotless:apply`), and enforcer complaints.
- Re-run `./mvnw verify` until it passes.
- Report what failed, what you changed, and the final result.
- Do not commit unless explicitly asked.
