/**
 * This package contains various classes and functions that may be useful for
 * functional programming.
 * 
 * <h3>About various functions</h3>
 * <h4>flatMap()</h4>
 * <ol>
 * <li>{@code flatMap()} is sometimes also called {@code bind()},
 * {@code andThen()} or {@code ifSomeDo()}.
 * <p>
 * Cf. reference WLA1.
 * </ol>
 * <h3>Railway Programming</h3>
 * <h4>Preliminary definition and concepts</h4>
 * In the following, {@code Either} instance means an
 * instance of any type that
 * is conceptually a specialization of {@code Either}:
 * {@link eu.infolead.jtk.fp.Maybe},
 * {@link eu.infolead.jtk.fp.Result}...
 * <p>
 * {@link eu.infolead.jtk.fp.Maybe#none()} and
 * {@link eu.infolead.jtk.fp.Result#failure(Anomaly)}
 * correspond to the <em>left</em> part of an {@code Either}.
 * <h4>Rail switch: from one track to two tracks</h4>
 * <p>
 * The creation of an {@code Either} instance from a single value corresponds to
 * the creation of a rail switch - we go from one-track functions to a two-track
 * functions.
 * <h4>Two tracks</h4>
 * {@code flatMap()} allows to take an {@code Either} instance
 * and to return an {@code Either} instance - we go from two-track functions to
 * two-track functions. For error handling: as soon as we have a left
 * {@code Either} instance, a call to {@code flatMap()} will always return this
 * left instance.
 * <h4>Back to one track</h4>
 * {@code ()} allows to receive either the value from the right side of the
 * {@code Either} instance if any, or the replacement value specified as a
 * parameter of
 * {@code or()}.
 * <p>
 * This is the correct way to retrieve a value, as it takes into
 * account the fact
 * that an {@code Either} has two possible forms/states (<em>left</em> or
 * <em>right</em>, <em>some</em> or
 * <em>none</em> for {@link eu.infolead.jtk.fp.Maybe}, <em>success</em> or
 * <em>failure</em> for
 * {@link eu.infolead.jtk.fp.Result}): not knowing what constitutes
 * a given {@code Either} instance (else one wouldn't need an {@code Either}
 * instance), it is necessary to think about the two
 * situations and handle each correctly.
 * <h3>References</h3>
 * <ol>
 * <li id="WLA1">WLA1: <em>Functional
 * Design Patterns (DevTernity 2018)</em>, Slide 90, <a href=
 * "https://www.slideshare.net/ScottWlaschin/functional-design-patterns-devternity2018">https://www.slideshare.net/ScottWlaschin/functional-design-patterns-devternity2018</a>,
 * Scott Wlaschin, 2018</li>
 * </ol>
 */
package eu.infolead.jtk.fp;
