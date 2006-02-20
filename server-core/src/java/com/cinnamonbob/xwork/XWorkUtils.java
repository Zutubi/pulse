package com.cinnamonbob.xwork;

import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;

/**
 * <class-comment/>
 */
public class XWorkUtils
{
    public static void install(String config)
    {
        ConfigurationManager.clearConfigurationProviders();
        ConfigurationManager.addConfigurationProvider(new XmlConfigurationProvider(config));
        ConfigurationManager.getConfiguration().reload();
    }
}
