package com.zutubi.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <class-comment/>
 */
@Constraint("com.zutubi.validation.validators.RequiredValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Required
{
    public static final String DEFAULT_messageKey = "";

    public static final String DEFAULT_defaultMessage = "";

    public static final boolean DEFAULT_shortCircuit = true;

    public static final boolean DEFAULT_ignorable = true;

    public String messageKey() default DEFAULT_messageKey;

    public String defaultMessage() default DEFAULT_defaultMessage;

    public boolean shortCircuit() default DEFAULT_shortCircuit;

    public boolean ignorable() default DEFAULT_ignorable;
}
