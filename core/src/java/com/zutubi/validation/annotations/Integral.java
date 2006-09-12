package com.zutubi.validation.annotations;

import com.zutubi.validation.validators.NumericValidator;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <class-comment/>
 */
@Constraint(handler = NumericValidator.class)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Integral
{
    int max() default Integer.MAX_VALUE;
    int min() default Integer.MIN_VALUE;
}
