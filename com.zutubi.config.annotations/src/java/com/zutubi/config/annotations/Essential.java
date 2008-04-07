package com.zutubi.config.annotations;

import com.zutubi.validation.annotations.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Similar to required, but specific to the config system and for application
 * to complex sub-properties.  For example, a project needs an SCM.  A
 * required validator is not helpful as configuring the SCM is a separate
 * step, so we use this validator to indicate that the project as a whole
 * should not be used until all essential pieces are provided.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint("com.zutubi.prototype.validation.EssentialValidator")
public @interface Essential
{
    static final String DEFAULT_defaultKeySuffix = "";

    static final boolean DEFAULT_shortCircuit = true;

    static final boolean DEFAULT_ignorable = true;

    String defaultKeySuffix() default DEFAULT_defaultKeySuffix;

    boolean shortCircuit() default DEFAULT_shortCircuit;

    boolean ignorable() default DEFAULT_ignorable;
}
