# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Architecture

This is the **Infolead Java Toolkit**, a modular Maven project providing functional programming utilities for Java 17+. The project follows a multi-module architecture:

- **jtk-parent**: Base parent POM with shared build configuration
- **jtk-dependencies**: Bill of Materials (BOM) for dependency management  
- **jtk-lang**: Core functional programming types (Either, Maybe, Result, TailCall), HTTP utilities, RFC references, and core functionality
- **jtk-currency**: Currency-related utilities

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

# Build all modules
mvn compile

# Run tests
mvn test

# Package artifacts
mvn package

# Install to local repository
mvn install

# Clean build artifacts
mvn clean
```

### Testing
- Uses JUnit 5 (Jupiter) with Spring Boot Test
- Test a single module: `mvn test -pl jtk-lang`
- Run specific test: `mvn test -Dtest=FactorialTest`

## Code Conventions

### Package Structure
- Root namespace: `eu.infolead.jtk`
- Functional programming: `eu.infolead.jtk.fp`
- Either types: `eu.infolead.jtk.fp.either`
- Language extensions: `eu.infolead.jtk.lang`

### Implementation Guidelines
- All APIs must support serialization for web sessions
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
- All dependency versions managed through `jtk-dependencies` BOM
- Add new modules to root `pom.xml` modules section

## Testing Strategy
All new code requires comprehensive test coverage using JUnit 5. Tests should cover both positive and negative cases, especially for the functional programming utilities that form the core of this toolkit.