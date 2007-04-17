package com.zutubi.validation.annotations;

import com.zutubi.validation.validators.URLValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})

/**
 */
@Constraint(URLValidator.class)
public @interface Url
{
}
