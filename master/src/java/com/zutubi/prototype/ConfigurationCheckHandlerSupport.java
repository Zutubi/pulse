package com.zutubi.prototype;

/**
 * Abstract base for configuration check handlers that supplies the standard
 * template names.
 */
public abstract class ConfigurationCheckHandlerSupport<T> implements ConfigurationCheckHandler<T>
{
    public String getSuccessTemplate()
    {
        return "prototype/check/success.ftl";
    }

    public String getFailureTemplate()
    {
        return "prototype/check/failure.ftl";
    }
}
