package com.zutubi.prototype.annotation;

import com.zutubi.prototype.ConfigurationCheckHandler;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationCheck
{
    Class<? extends ConfigurationCheckHandler> value();
}
