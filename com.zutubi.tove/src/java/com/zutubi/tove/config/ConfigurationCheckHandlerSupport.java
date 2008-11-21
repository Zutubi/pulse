package com.zutubi.tove.config;

import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.api.ConfigurationCheckHandler;

/**
 * Abstract base for configuration check handlers that supplies the standard
 * template names.
 */
public abstract class ConfigurationCheckHandlerSupport<T extends Configuration> extends AbstractConfiguration implements ConfigurationCheckHandler<T>
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
