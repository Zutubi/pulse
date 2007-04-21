package com.zutubi.config.annotations;

import com.zutubi.prototype.ConfigurationCheckHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

/**
 * The ConfigurationCheck annotation allows for a ConfigurationCheckHandler to be associated
 * with a configuration object.
 *
 * Configuration checks are presented in the UI as a separate form that can be used to run
 * a check on the target configuration.
 *
 */
public @interface ConfigurationCheck
{
    /**
     * The reference to a configuration check handler implementation. 
     * 
     * @return the type to be used to run the configuration check.
     *
     * @see ConfigurationCheckHandler
     */
    String value();
}
