/**
 * Test support for the Infolead Java toolkit, shipped as a library so downstream applications can
 * test their own jtk-based code with the same rigor the toolkit applies to itself.
 *
 * <p>The module provides four facilities:
 *
 * <ul>
 *   <li>{@link eu.infolead.jtk.test.JtkAssertions AssertJ assertions} that speak the jtk
 *       vocabulary: {@code assertThat(result).isSuccess().hasValue(order)} instead of decoding
 *       {@link eu.infolead.jtk.fp.either.Result} internals at every call site.
 *   <li>{@link eu.infolead.jtk.test.error.TestErrors Error fixtures} and the {@link
 *       eu.infolead.jtk.test.error.ErrorFixture builder}: canonical, deterministic {@link
 *       eu.infolead.jtk.anomaly.SystemError} instances whose volatile error IDs are pinned, which
 *       makes RFC&nbsp;9457 problem+json snapshot assertions possible.
 *   <li>{@link eu.infolead.jtk.test.gen.JtkArbitraries jqwik arbitraries} for {@code Result},
 *       {@code Maybe}, {@code Either}, {@code Validation} and {@code Bool}, so property tests can
 *       generate container values, including the empty-success variant.
 *   <li>{@link eu.infolead.jtk.test.serial.RoundTrip serialization round-trip helpers} for the
 *       toolkit's two serialization contracts: session replication ({@code java.io.Serializable})
 *       and the JSON wire envelope.
 * </ul>
 *
 * <p>Dependency direction: this module sits <em>above</em> {@code jtk-lang} ({@code jtk-test &rarr;
 * jtk-lang}); it can therefore never be a test dependency of {@code jtk-lang} itself. The toolkit's
 * own conformance suite &mdash; the functor/monad law tests for the container types and the
 * serialization round-trip properties &mdash; lives in this module's test tree for exactly that
 * reason, mirroring the TCK pattern.
 *
 * <p>This is a test-scope artifact: consumers MUST declare it with {@code <scope>test</scope>} and
 * MUST NOT put it on a runtime classpath. It deliberately pulls AssertJ, jqwik and Jackson&nbsp;3
 * at compile scope so they flow into the consumer's test classpath through this one dependency.
 */
package eu.infolead.jtk.test;
