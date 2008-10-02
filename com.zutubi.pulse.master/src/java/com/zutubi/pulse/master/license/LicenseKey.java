package com.zutubi.pulse.master.license;

import com.zutubi.validation.annotations.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 *
 */
@Constraint("com.zutubi.pulse.master.license.LicenseKeyValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LicenseKey
{
    public static final String DEFAULT_messageKey = "";

    public static final String DEFAULT_defaultMessage = "";

    public static final boolean DEFAULT_shortCircuit = true;

    public String messageKey() default DEFAULT_messageKey;

    public String defaultMessage() default DEFAULT_defaultMessage;

    public boolean shortCircuit() default DEFAULT_shortCircuit;
}
