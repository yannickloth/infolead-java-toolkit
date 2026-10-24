# Serialization and the module boundary

This document records the two serialization contracts of the toolkit, where each
one is implemented, and why the implementation is split across artifacts the way
it is. It is the durable answer to the review finding that
[`CLAUDE.md`](CLAUDE.md) required serialization for web sessions while only a
fraction of the API actually provided it.

## Two contracts, not one

"Serialization" was one bullet point; it is really two independent contracts
with different drivers.

| Contract | Driver for change | Enforced by |
| --- | --- | --- |
| **Session persistence** (`java.io.Serializable`) | Servlet-container session replication, clustering, RMI | `ValueContainer`, `SystemError`, `ValidationResult`, `ErrorContext`, `RedactionPolicy` |
| **JSON wire shape** | the published API, RFC 9457, Jackson's annotation model | Jackson annotations on the types, pinned by tests |

They are independent: a Jackson release cannot break session replication, and a
change to the wire shape does not change `serialVersionUID`. Treating them as one
requirement is what produced the original gap.

### Session persistence

All instances of the `Either` hierarchy (`Either`, `Maybe`, `Result`,
`Validation`) are serializable through `ValueContainer`, provided the payload is
serializable. In addition:

- `ValidationResult` and its records are serializable.
- The whole `SystemError` hierarchy is serializable, including
  `ContextualSystemError`, `ErrorContext` and `RedactionPolicy`.
- `jtk-currency`'s `Currency` already used a serialization proxy and needed no
  change.

The payload is always the caller's responsibility: a `Result<MyError, Object>`
holding a non-serializable object cannot be replicated. The types document that
requirement rather than hiding it.

### JSON wire shape

The shape is declared by annotations in `jtk-lang` only, so it holds with a plain
`ObjectMapper` and **without registering a module**. This matters because the
library is published for consumers who configure their own mapper.

Every `Either` is a single-member envelope. `left` is the failure/invalid/absent
branch and `right` is the success/present branch.

| Type | JSON | Notes |
| --- | --- | --- |
| `Either<L,R>` | `{"left":…}` or `{"right":…}` | both branches, or neither, are rejected |
| `Maybe<T>` | `{"right":value}` or `{"left":null}` | `{}` reads as `none()`; absent right means absence |
| `Result<F,S>` | `{"left":anomaly}`, `{"right":value}`, or `{"right":null}` | `{"right":null}` is `EmptySuccess`; an absent or null `left` member means no failure, so `{}` also reads as empty success |
| `Validation<I,V>` | `{"left":error}` or `{"right":value}` | neither branch is rejected |
| `ValidationResult<T>` | `{"right":value}` or `{"left":[errors]}` | `{}` is rejected; an empty `left` means success |

The payload type is taken from the generic parameters the consumer declares
(`Either<SystemError, Order>`), so no type hints are needed and no polymorphic
type ids are written. The one asymmetry is deliberate: because `Some`, `Success`,
`Valid` and `Failure` reject a null payload, a null `right` member carries no
value and is therefore unambiguous.

Contract rules that are pinned by tests:

1. `{"left":…}` and `{"right":…}` together are rejected for `Either`, `Result`
   and `Validation`.
2. An empty envelope is rejected for `Either` and `Validation`.
3. `Maybe.none()` and `Result.success()` keep their singleton identity through
   both Java serialization and JSON.
4. The raw `value()` accessor of a container is never serialized.

### Error documents

`SystemError` writes a `kind` discriminator plus the record components. Error
types are written as their **stable message key**
(`error.validation.parameter.null-parameter`), not as enum constant names, and
are resolved back from that key. Unknown keys are rejected rather than defaulted
to, so contract drift cannot silently produce a wrongly classified error.

Derived members (`message`, `severity`, `httpStatus`, `clientError`,
`serverError`, `category`) are also written because they are what a client needs;
they are read-only and ignored on the way back in.

Operational metadata is deliberately **not** on the wire:
`ContextualSystemError` serializes only its `delegate`. Cause chains, suppressed
exceptions, correlation context and the redaction policy stay in the process that
observed the failure — they contain throwables, tenant identifiers and strategy
objects that must not cross a trust boundary.

## Module layout

```
jtk-lang                     contract: types, Serializable, annotations, tests
  ^
jtk-jackson3  (primary)      databind mechanics + problem+json media type
jtk-jackson2  (bridge)       same, on the Jackson 2 API; deprecated at release
  ^
jtk-spring-autoconfigure     Spring Boot 4 auto-config + Spring ProblemDetail bridge
```

Each artifact changes for exactly one reason:

| Artifact | Changes when |
| --- | --- |
| `jtk-lang` | the type algebra, the session-persistence requirement, or the wire contract changes |
| `jtk-jackson3` | Jackson 3's databind API changes |
| `jtk-jackson2` | Jackson 2's databind API changes |
| `jtk-spring-autoconfigure` | Spring Boot or Spring Framework changes |

The edges point from volatile to stable, so a Jackson or Spring upgrade can never
force a core release. The reverse split — putting the wire mapping in a Jackson
artifact — would have made every `ProblemDetail` change a coordinated release of
two artifacts, and would have given consumers who do not register the module no
guarantee about the JSON their own types produce.

Jackson 2 and Jackson 3 cannot share serializer code: Jackson 3 moved
`core`/`databind` to `tools.jackson.*` and renamed `JsonSerializer` to
`ValueSerializer`. `jackson-annotations` stayed at
`com.fasterxml.jackson.annotation`, which is why the contract layer is portable
across both majors and only the mechanics are duplicated.

### What the modules actually own

The annotation contract turned out to express nearly everything, so the modules
are deliberately small rather than speculative:

- `jtk-jackson3` / `jtk-jackson2`: a `ProblemDetail` deserializer for the sealed
  interface (the two permitted shapes are distinguished by the `multistatus`
  member, which annotations cannot express), the `application/problem+json`
  media type and its reader/writer, and a preconfigured mapper factory.
- `jtk-spring-autoconfigure`: the Boot 4 `JsonMapperBuilderCustomizer` that
  registers the module (Boot 4 collects customizers, not `JacksonModule` beans),
  the module bean, and a translation between the toolkit's `ProblemDetail` and
  `org.springframework.http.ProblemDetail`.

If the mechanics ever shrink to nothing, delete the module rather than keeping an
empty artifact: an artifact that exists only to exist is a release cost.

## Quarkus: deliberately deferred

No Quarkus extension is shipped yet. The reason is technical, not scheduling:

- Quarkus 3.37 enables **reflection-free Jackson serializers by default**
  (build-time generated serializers; first on in 3.35, reverted in 3.36.3,
  re-enabled in 3.37). That generation is driven by the annotation model, which
  is why the annotation-only core contract is the right investment: it is
  already the maximum Quarkus-compatible thing that can be done.
- A working extension is not a thin customizer. It needs a deployment module that
  participates in serializer generation and reflection registration for the
  containers and for every consumer payload type. That is a standing maintenance
  contract with Quarkus' release cadence.
- `io.quarkus.jackson.ObjectMapperCustomizer` is a JVM-mode mechanism and does
  nothing in a native image.

Revisit this when native support is an actual requirement. The contract does not
need to change to do so.

## Version matrix

| Component | Version |
| --- | --- |
| Java | 17 |
| Jackson annotations (shared) | 2.22.4 (managed by `jtk-parent`) |
| Jackson 3 databind | 3.0.2 |
| Jackson 2 databind (bridge) | 2.22.4 |
| Spring Boot auto-configuration | 4.0.0 |

`jtk-lang` compiles against `jackson-annotations` only. `jackson-databind` is a
test-scope dependency there, on purpose: the tests are the specification of the
wire contract and therefore live with the types.

## Where the guarantees are tested

| Test | Guarantee |
| --- | --- |
| `jtk-lang` …`JsonEnvelopeContractTest` | the envelope shape and rules with a plain mapper |
| `jtk-lang` …`SystemErrorJsonContractTest` | `kind`, error-type keys, and Java-serialization round-trips |
| `jtk-lang` …`ModuleBoundaryTest` | `jtk-lang` stays framework-free and annotation-only |
| `jtk-jackson3` …`JtkJacksonContractTest` | interface-shaped `ProblemDetail`, media type, module interaction |
| `jtk-jackson2` …`JtkJacksonContractTest` | the same mechanics on Jackson 2 |
| `jtk-spring-autoconfigure` …`JtkJacksonAutoConfigurationTest` | Boot 4 wiring and the Spring `ProblemDetail` bridge |

## Adding a new type or a new module

1. Declare the wire shape with annotations in `jtk-lang` and add a round-trip
   test with a plain mapper. If it only works with a registered module, the
   shape is not part of the published contract.
2. Never use `@JsonSerialize`/`@JsonDeserialize` in `jtk-lang`; they need
   `jackson-databind` at compile scope and `ModuleBoundaryTest` will fail.
3. Add mechanics to `jtk-jackson3` (and the bridge), not to the core.
4. Add a framework artifact per framework, not per Jackson major. Quarkus and
   Spring release independently of Jackson.
