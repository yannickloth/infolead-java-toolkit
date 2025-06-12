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

### Module Dependencies
- New modules should inherit from `jtk-parent`
- All dependency versions managed through `jtk-dependencies` BOM
- Add new modules to root `pom.xml` modules section

## Testing Strategy
All new code requires comprehensive test coverage using JUnit 5. Tests should cover both positive and negative cases, especially for the functional programming utilities that form the core of this toolkit.