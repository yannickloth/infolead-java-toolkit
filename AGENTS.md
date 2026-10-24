# AGENTS.md

This file provides guidance to AI coding agents (OpenCode) when working with code in this repository.

## Project Architecture

This is the **Infolead Java Toolkit**, a modular Maven project providing functional programming utilities for Java 17+. The project follows a multi-module architecture:

- **jtk-parent**: Base parent POM with shared build configuration
- **jtk-bom** (directory `jtk-dependencies`): Bill of Materials (BOM) for toolkit artifacts
- **jtk-lang**: Core functional programming types (Either, Maybe, Result, TailCall), HTTP utilities, RFC references, and core functionality
- **jtk-async**: Asynchronous railway: CompletableFuture/Result bridging, bounded parallel traversal, deadlines, retries and cancellation
- **jtk-currency**: Currency-related utilities
- **jtk-jackson3**: Jackson 3 integration; depends on jtk-lang only, no framework dependency. JPMS module name: `eu.infolead.jtk.jackson.v3`
- **jtk-jackson2**: Jackson 2 bridge mirroring jtk-jackson3 on the `com.fasterxml.jackson` API (deprecated at release). JPMS module name: `eu.infolead.jtk.jackson.v2`
- **jtk-spring-autoconfigure**: Spring Boot auto-configuration for the Jackson module and the RFC 9457 problem+json bridge; the only framework-dependent artifact. Classpath-only (no `module-info.java`) because Spring Boot autoconfiguration is not module-friendly
- **jtk-test**: Test support library: AssertJ assertions for jtk types, deterministic anomaly fixtures, jqwik arbitraries and serialization round-trip helpers; hosts the toolkit's own law conformance suite. Consumers declare it with test scope only

### Key Design Patterns
- **Railway Oriented Programming**: Implemented via Either types (`Either<L,R>`, `Maybe<T>`, `Result<T>`, `Validation<T>`)
- **Monadic Operations**: All types support `map()`, `flatMap()`, `fold()`, and `bind()`
- **Tail Call Optimization**: Custom `TailCall<T>` interface to work around Java's lack of TCO
- **Sealed Interfaces**: Extensive use for type safety and pattern matching readiness

## Development Environment

### Setup Commands
```bash
# Enter Nix development shell (required)
nix-shell

# Build all modules (Maven 3.9+; the enforcer rejects older versions)
./mvnw compile

# Run tests
./mvnw test

# Package artifacts
./mvnw package

# Full verification (tests, Spotless, JaCoCo, enforcer)
./mvnw verify

# Install to local repository
./mvnw install

# Clean build artifacts
./mvnw clean
```

### Testing
- Uses JUnit 5 (Jupiter) with Spring Boot Test
- Test a single module: `./mvnw test -pl jtk-lang`
- Run a single test class: `./mvnw test -pl jtk-lang -Dtest=FactorialTest`

### Verification
- `./mvnw verify` runs the tests, the Spotless check, JaCoCo reporting, and the Maven enforcer
- Spotless enforces google-java-format (AOSP style) on files changed relative to `origin/main`; fix violations with `./mvnw spotless:apply`
- JaCoCo LINE coverage gate is set to `0.70` for every published module
- Javadoc is built with `doclint` set to `all,-missing`; release dry-run with `./mvnw clean verify -Prelease -Dgpg.skip=true`
- The OpenCode slash commands `/verify`, `/test`, and `/format` wrap these flows (defined in `.opencode/commands/`)

## Code Conventions

### Package Structure
- Root namespace: `eu.infolead.jtk`
- Functional programming: `eu.infolead.jtk.fp`
- Either types: `eu.infolead.jtk.fp.either`
- Language extensions: `eu.infolead.jtk.lang`

### Implementation Guidelines
- All APIs must support serialization, which is **two independent contracts**: session replication (`java.io.Serializable`) and a JSON wire shape that holds **without registering a module**. See `SERIALIZATION_AND_MODULES.md` for the envelope rules, the error-document contract and the module boundaries
- `jtk-lang` stays framework-free and annotation-only: Jackson annotations, never `@JsonSerialize`/`@JsonDeserialize`, never `jackson-databind` at compile scope. `ModuleBoundaryTest` enforces this
- Follow RFC 2119 compliance for documentation keywords (MUST, SHOULD, etc.)
- Use sealed interfaces for sum types
- Implement comprehensive JavaDoc with academic references where applicable
- Maintain immutability and functional programming principles

### Control Flow: `fold()` vs `if/else`

`fold()` is the **expression** form of branching. It is not a replacement for `if/else`.

- **Use `if/else` for statements**: guard clauses, early returns, throwing, logging,
  mutation, and any other side effect. Idiomatic form for these types is
  `if (bool.toBoolean()) { ... }`.
- **Use `fold()` only when the result is a value** of type `R`, both arms are pure and
  symmetric, and the arms read as self-describing method references:
  `bool.fold(this::renderNone, this::renderValue)`.
- **Use the ternary** (`cond ? a : b`) when both operands are already cheap values.
  Java evaluates only the taken branch of a ternary, so `fold()` adds no laziness there
  and costs two `Supplier`s plus an indirection.
- **Never `fold()` to run statements.** An arm written as `() -> { doSideEffect(); return null; }`
  is a sign the code wanted `if/else`.
- **`ifTrue()`, `ifFalse()`, `ifNull()` and `apply(...)`** are intentional side-effect
  APIs. Prefer `if/else` over them unless a fluent `Bool`/`NullableBool` chain is already
  established in the surrounding code.
- **Argument order is `fold(false, [null,] true)`.** Swapping the arms compiles and
  silently inverts behaviour when both arms return the same type. This is a further
  reason to prefer `if/else` for non-trivial arms.
- **Do not wrap a primitive `boolean` in `Bool.of(cond)` purely to branch on it.** Use `if`.
- For `NullableBool`, `fold(false, null, true)` is worth more than for `Bool`: the
  three-valued case analysis has no total `if/else` equivalent, so `fold()` forces the
  `NULL` arm to be handled.

See `eu.infolead.jtk.logic.package-info` for the full guidance and examples.

### Module Dependencies
- New modules should inherit from `jtk-parent`
- All dependency versions managed through the `jtk-bom` BOM (directory `jtk-dependencies`)
- Add new modules to root `pom.xml` modules section

## Testing Strategy
All new code requires comprehensive test coverage using JUnit 5. Tests should cover both positive and negative cases, especially for the functional programming utilities that form the core of this toolkit.

## Reference Documents
- `SERIALIZATION_AND_MODULES.md`: the session-persistence and JSON contracts, the module/volatility boundaries, and the deferred Quarkus decision
- `UNIFIED_ERROR_SYSTEM.md`: design of the unified error system — error hierarchy, HTTP/RFC 7807 ProblemDetail mapping, and error type number ranges
- `EXAMPLE_ERROR_IDS.md`: worked examples of the error ID system
