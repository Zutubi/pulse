package com.zutubi.validation.annotations;

import com.zutubi.validation.Validator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <class-comment/>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Constraint
{
    Class<? extends Validator> handler();
}
