package com.zutubi.pulse.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SecureParameter
{
    public int parameterIndex() default -1;
    public Class parameterType() default Object.class;
    public String action();
}
