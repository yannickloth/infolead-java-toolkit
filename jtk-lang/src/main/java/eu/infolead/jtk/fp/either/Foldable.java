package eu.infolead.jtk.fp.either;

import eu.infolead.jtk.fp.Mapper;

/**
 * Interface for foldables with two possible values.
 */
public interface Foldable<L, R> {

    <U> U fold(Mapper<? extends U, ? super L> leftMapper, Mapper<? extends U, ? super R> rightMapper);

}
