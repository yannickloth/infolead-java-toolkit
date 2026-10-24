package eu.infolead.jtk.fp.either;

import eu.infolead.jtk.fp.Mapper;

/**
 * A value that can be folded into a single result by handling both of its
 * possible cases.
 * <p>
 * Implementations must invoke exactly one of the supplied mappers, and must
 * return the result of that mapper.
 *
 * @param <L> the type of the left (usually error) value
 * @param <R> the type of the right (usually success) value
 */
public interface Foldable<L, R> {

    /**
     * Folds this value by applying the mapper matching its case.
     *
     * @param <U>          the type of the result
     * @param leftMapper   the mapper applied to the left value
     * @param rightMapper  the mapper applied to the right value
     * @return the result of the applied mapper
     */
    <U> U fold(Mapper<? extends U, ? super L> leftMapper, Mapper<? extends U, ? super R> rightMapper);

}
