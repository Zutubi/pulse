package com.zutubi.tove.config.api;

/**
 * Abstract base for configuration check handlers that supplies the standard
 * template names.
 */
public abstract class AbstractConfigurationCheckHandler<T extends Configuration> extends AbstractConfiguration implements ConfigurationCheckHandler<T>
{
    public String getSuccessTemplate()
    {
        return "tove/check/success.ftl";
    }

    public String getFailureTemplate()
    {
        return "tove/check/failure.ftl";
    }
}
