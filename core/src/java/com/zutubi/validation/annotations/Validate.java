package com.zutubi.validation.annotations;

import com.zutubi.validation.validators.RequiredValidator;
import com.zutubi.validation.validators.DelegateValidator;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <class-comment/>
 */
@Constraint( handler = DelegateValidator.class )
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate
{
}
