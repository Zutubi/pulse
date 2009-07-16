package com.zutubi.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks the field as a numeric field.  Optionally, the
 * numeric value can be constrained by the min and max constraints.
 * <p>
 * This annotation can be applied to integral and string fields.
 * <p>
 * For example:
 *
 * <pre><code>&#064;Numeric(min = 0, max = 100)
 * public String getNumber()
 * {
 *     return "093";
 * }</code></pre>
 */
@Constraint("com.zutubi.validation.validators.NumericValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Numeric
{
    static final String DEFAULT_defaultKeySuffix = "";

    static final boolean DEFAULT_shortCircuit = true;

    static final int DEFAULT_max = Integer.MAX_VALUE;

    static final int DEFAULT_min = Integer.MIN_VALUE;

    String defaultKeySuffix() default DEFAULT_defaultKeySuffix;

    boolean shortCircuit() default DEFAULT_shortCircuit;

    int max() default DEFAULT_max;

    int min() default DEFAULT_min;
}


