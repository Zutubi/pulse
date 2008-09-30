package com.zutubi.pulse.security;

import com.zutubi.tove.security.AccessManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SecureResult
{
    public String value() default AccessManager.ACTION_VIEW;
}
