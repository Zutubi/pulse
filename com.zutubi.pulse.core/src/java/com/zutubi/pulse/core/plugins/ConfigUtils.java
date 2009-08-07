package com.zutubi.pulse.core.plugins;

import com.zutubi.util.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Utility classes for dealing with the configuration stored in plugin.xml
 * files.
 */
public class ConfigUtils
{
    public static String getString(IConfigurationElement config, String attribute, String defaultValue)
    {
        String value = config.getAttribute(attribute);
        if(StringUtils.stringSet(value))
        {
            return value;
        }
        else
        {
            return defaultValue;
        }
    }

    public static boolean getBoolean(IConfigurationElement config, String attribute, boolean defaultValue)
    {
        String value = config.getAttribute(attribute);
        if(StringUtils.stringSet(value))
        {
            return Boolean.parseBoolean(value);
        }
        else
        {
            return defaultValue;
        }
    }
}
