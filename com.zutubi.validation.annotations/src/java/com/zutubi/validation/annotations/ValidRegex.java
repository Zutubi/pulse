package com.zutubi.validation.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * For string fields that must be compilable as a regular expression.
 */
@Constraint("com.zutubi.validation.validators.ValidRegexValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRegex
{
}
