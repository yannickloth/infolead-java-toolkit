---
description: Apply Spotless formatting (google-java-format AOSP)
---

Run `./mvnw spotless:apply` from the repository root, then summarize which files were reformatted.

Note: the Spotless `check` bound to `verify` only ratchets files changed relative to `origin/main`, so untouched files will not fail the build.
