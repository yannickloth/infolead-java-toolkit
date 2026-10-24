/**
 * Two-valued ({@link eu.infolead.jtk.logic.Bool}) and three-valued
 * ({@link eu.infolead.jtk.logic.NullableBool}) booleans, with the classical
 * connectives {@code and}, {@code or}, {@code xor} and {@code negate}.
 *
 * <h2>Choosing between {@code fold()} and {@code if/else}</h2>
 *
 * <p>{@code fold()} is the <em>expression</em> form of branching, not a replacement for
 * {@code if/else}. It exists to produce a value, not to run statements.</p>
 *
 * <h3>Use {@code if/else} when</h3>
 * <ul>
 *   <li>The branch is a statement: a guard clause, an early return, a {@code throw},
 *       a log call, an assignment, or any other side effect.</li>
 *   <li>Only one branch does interesting work and the other is simply "do nothing".</li>
 *   <li>The code is on a hot path: {@code fold()} takes {@link java.util.function.Supplier}s, so each call
 *       site adds indirection and, for capturing lambdas, an allocation.</li>
 *   <li>Readable stack traces and step-through debugging matter.</li>
 * </ul>
 * <p>The idiomatic form for these types is
 * {@code if (bool.toBoolean()) { ... }}. Do not write
 * {@code Bool.of(condition).fold(...)} when you already hold a primitive
 * {@code boolean}: wrapping it purely to branch on it is pure overhead.</p>
 *
 * <h3>Use {@code fold()} when</h3>
 * <ul>
 *   <li>The result is a value of type {@code R}, and the branch is part of an
 *       expression (a return value, an argument, a chain link).</li>
 *   <li>Both arms are pure, cheap and symmetric, ideally named method references
 *       that read as self-documenting: {@code fold(this::renderNone, this::renderValue)}.</li>
 *   <li>The surrounding code is already a fluent functional pipeline, and breaking out
 *       into a statement would interrupt it.</li>
 * </ul>
 *
 * <h3>Use the ternary when</h3>
 * <p>Both operands are already cheap values and the condition is a plain
 * {@code boolean}: {@code cond ? a : b}. Java evaluates only the taken branch of a
 * ternary, so {@code fold()} buys no additional laziness, and its extra arguments make
 * the intent less obvious at the call site.</p>
 *
 * <h3>Anti-patterns</h3>
 * <pre>{@code
 * // Bad: fold used to run statements. This wanted if/else.
 * Bool.of(valid).fold(
 *         () -> { log.warn("invalid"); return null; },
 *         () -> { metrics.increment(); return null; });
 *
 * // Bad: null arms. fold() only validates the arm it takes, so a null arm for the
 * // untaken branch stays silently undetected.
 * flag.fold(null, () -> value);
 *
 * // Bad: wrapping a primitive just to branch on it.
 * if (Bool.of(count > 0).toBoolean()) { ... }
 * }</pre>
 *
 * <h3>Argument order</h3>
 * <p>{@link eu.infolead.jtk.logic.Bool#fold(java.util.function.Supplier, java.util.function.Supplier)}
 * takes {@code fold(falseSupplier, trueSupplier)}, and
 * {@link eu.infolead.jtk.logic.NullableBool#fold(java.util.function.Supplier, java.util.function.Supplier, java.util.function.Supplier)}
 * takes
 * {@code fold(falseSupplier, nullSupplier, trueSupplier)}. Swapping the arms compiles
 * and, when both arms return the same type, silently inverts behaviour. Treat the
 * argument order as the main footgun of this API and prefer {@code if/else} when the
 * arms are not trivially readable.</p>
 *
 * <h3>Side-effect APIs</h3>
 * <p>{@code ifTrue}, {@code ifFalse}, {@code ifNull} and {@code apply} are the intended
 * side-effect entry points, and are chainable because they return the receiver. Prefer
 * {@code if/else} over them unless a fluent {@code Bool}/{@code NullableBool} chain is
 * already the style of the surrounding code: introducing one of these at a single
 * statement-shaped call site adds a third idiom with no benefit.</p>
 *
 * <h3>Where {@code fold()} earns its keep</h3>
 * <p>For {@link eu.infolead.jtk.logic.Bool}, {@code fold()} carries no exhaustiveness
 * advantage over {@code if/else}: a classical conditional is already total. Its value is
 * purely as a named, typed expression inside a pipeline.</p>
 * <p>For {@link eu.infolead.jtk.logic.NullableBool} the situation is different, and
 * {@code fold(false, null, true)} is meaningfully stronger than {@code if/else}: a
 * three-valued case analysis has no total statement form, so {@code fold()} makes the
 * {@code NULL} arm impossible to forget. Prefer it whenever all three states must
 * produce a value.</p>
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // Expression with named, symmetric arms -> fold()
 * String text = actualValue.fold(() -> "none", SystemError::renderValue);
 *
 * // Three-valued total case analysis -> fold()
 * HttpStatus status = nullableBool.fold(() -> Status.GONE, () -> Status.UNKNOWN, () -> Status.OK);
 *
 * // Guard clause / side effect -> if/else
 * if (!HttpStatusCode.isValid(code).toBoolean()) {
 *     throw new ParameterError(...);
 * }
 *
 * // Cheap value choice -> ternary
 * return present ? rendered : "none";
 * }</pre>
 *
 * @see eu.infolead.jtk.logic.Bool
 * @see eu.infolead.jtk.logic.NullableBool
 * @see eu.infolead.jtk.fp.either
 */
package eu.infolead.jtk.logic;
