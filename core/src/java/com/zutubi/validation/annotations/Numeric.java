package com.zutubi.validation.annotations;

import com.zutubi.validation.validators.NumericValidator;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <class-comment/>
 */
@Constraint(NumericValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Numeric
{
    public static final int DEFAULT_max = Integer.MAX_VALUE;

    public static final int DEFAULT_min = Integer.MIN_VALUE;

    int max() default DEFAULT_max;

    int min() default DEFAULT_min;
}
