/**
 * Functional programming utilities and error handling framework.
 * 
 * <p>This package contains various classes and functions that may be useful for
 * functional programming, including a comprehensive error handling system that
 * distinguishes between validation errors and contract violations.</p>
 * 
 * <h2>Error Handling Strategy</h2>
 * 
 * <p>This package provides two distinct error handling approaches:</p>
 * 
 * <h3>1. Validation Errors ({@link eu.infolead.jtk.fp.validation.ValidationError})</h3>
 * <ul>
 *   <li><strong>Purpose:</strong> Handle expected user input errors and business rule violations</li>
 *   <li><strong>Examples:</strong> Invalid email format, required field missing, value out of range</li>
 *   <li><strong>Response:</strong> Return error messages to users, allow retry</li>
 *   <li><strong>HTTP Status:</strong> 400 Bad Request, 422 Unprocessable Entity</li>
 *   <li><strong>Usage:</strong> Web API validation, form processing, business rule checking</li>
 * </ul>
 * 
 * <h3>2. Contract Errors ({@link eu.infolead.jtk.fp.ContractError})</h3>
 * <ul>
 *   <li><strong>Purpose:</strong> Handle programming errors and contract violations</li>
 *   <li><strong>Examples:</strong> Null parameter passed, method precondition violated, invariant broken</li>
 *   <li><strong>Response:</strong> Fast-fail with exceptions, indicates bugs in code</li>
 *   <li><strong>HTTP Status:</strong> 500 Internal Server Error</li>
 *   <li><strong>Usage:</strong> Design by Contract, defensive programming, API contract enforcement</li>
 * </ul>
 * 
 * <h2>Railway-Oriented Programming</h2>
 * 
 * <h3>About various functions</h3>
 * <h4>{@link eu.infolead.jtk.fp.either.Either#map()}</h4>
 * With {@link eu.infolead.jtk.fp.either.Either#map()}, the specified mapping function
 * is applied on the value and returns a new value ({@code flatMap()} returns a
 * new instance of {@link Either}).
 * <h4>{@link eu.infolead.jtk.fp.either.Either#flatMap()}</h4>
 * <ol>
 * <li>{@code flatMap()} is sometimes also called {@code bind()},
 * {@code andThen()} or {@code ifSomeDo()}.
 * <li>
 * With {@link eu.infolead.jtk.fp.either.Either#flatMap()}, the specified mapping
 * function
 * is applied on the value and returns a new {@link Either} instance.
 * </li>
 * <p>
 * Cf. reference WLA1.
 * </ol>
 * <h3>Two-track processing</h3>
 * <h4>Preliminary definition and concepts</h4>
 * In the following, {@code Either} instance means an
 * instance of any type that
 * is conceptually a specialization of {@code Either}:
 * {@link eu.infolead.jtk.fp.either.Maybe},
 * {@link eu.infolead.jtk.fp.either.Result}...
 * <p>
 * {@link eu.infolead.jtk.fp.either.Maybe#none()} and
 * {@link eu.infolead.jtk.fp.either.Result#failure(Object)}
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
 * {@code or()} allows to receive either the value from the right side of the
 * {@code Either} instance if any, or the replacement value specified as a
 * parameter of {@code or()}.
 * <p>
 * This is the correct way to retrieve a value, as it takes into
 * account the fact
 * that an {@code Either} has two possible forms/states (<em>left</em> or
 * <em>right</em>, <em>some</em> or
 * <em>none</em> for {@link eu.infolead.jtk.fp.either.Maybe}, <em>success</em> or
 * <em>failure</em> for
 * {@link eu.infolead.jtk.fp.either.Result}): not knowing what constitutes
 * a given {@code Either} instance (else one wouldn't need an {@code Either}
 * instance), it is necessary to think about the two
 * situations and handle each correctly.
 * 
 * <h2>Validation vs Contract Error Decision Guide</h2>
 * 
 * <table border="1">
 * <tr>
 *   <th>Scenario</th>
 *   <th>Error Type</th>
 *   <th>Rationale</th>
 * </tr>
 * <tr>
 *   <td>User enters invalid email format</td>
 *   <td>ValidationError</td>
 *   <td>Expected user input error, should be handled gracefully</td>
 * </tr>
 * <tr>
 *   <td>API receives null for required parameter</td>
 *   <td>ContractError</td>
 *   <td>API contract violation, indicates client programming error</td>
 * </tr>
 * <tr>
 *   <td>Form field exceeds max length</td>
 *   <td>ValidationError</td>
 *   <td>Business rule violation, user can correct and retry</td>
 * </tr>
 * <tr>
 *   <td>Method called with negative array index</td>
 *   <td>ContractError</td>
 *   <td>Precondition violation, programming error</td>
 * </tr>
 * </table>
 * 
 * <h3>References</h3>
 * <ol>
 * <li id="WLA1">WLA1: <em>Functional
 * Design Patterns (DevTernity 2018)</em>, Slide 90, <a href=
 * "https://www.slideshare.net/ScottWlaschin/functional-design-patterns-devternity2018">https://www.slideshare.net/ScottWlaschin/functional-design-patterns-devternity2018</a>,
 * Scott Wlaschin, 2018</li>
 * </ol>
 */
package eu.infolead.jtk.fp;

import eu.infolead.jtk.fp.either.Either;
