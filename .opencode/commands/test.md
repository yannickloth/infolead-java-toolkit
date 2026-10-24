---
description: Run Maven tests for a module or a single test class
---

Run `./mvnw test $ARGUMENTS` from the repository root. If no arguments were given, run the full suite.

Useful argument shapes:

- One module: `-pl jtk-lang`
- One test class: `-pl jtk-lang -Dtest=FactorialTest`

Diagnose and fix any failures, then re-run to confirm. Report what failed and what you changed.
