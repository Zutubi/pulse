package com.zutubi.pulse.core.config;

/**
 * Abstract base for configuration check handlers that supplies the standard
 * template names.
 */
public abstract class ConfigurationCheckHandlerSupport<T extends Configuration> extends AbstractConfiguration implements ConfigurationCheckHandler<T>
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
