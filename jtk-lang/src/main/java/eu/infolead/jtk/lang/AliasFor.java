package eu.infolead.jtk.lang;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a type, respectively a method, is an alias for another type,
 * respectively another method.
 * <p>
 * The annotation is informational: it is retained at runtime for tooling, but
 * Java does not synthesize the aliased member automatically. Every alias must
 * still delegate to its target explicitly.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Documented
public @interface AliasFor {
    /**
     * The type declaring the aliased member.
     *
     * @return the declaring type
     */
    Class<?> type();

    /**
     * The name of the aliased method, or an empty string when the annotation
     * applies to a type.
     *
     * @return the aliased method name
     */
    String method() default "";
}
