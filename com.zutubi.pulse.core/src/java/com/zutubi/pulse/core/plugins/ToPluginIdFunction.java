package com.zutubi.pulse.core.plugins;

import com.google.common.base.Function;

/**
 * A mapping that maps a plugin to its id.
 */
public class ToPluginIdFunction implements Function<Plugin, String>
{
    public String apply(Plugin plugin)
    {
        return plugin.getId();
    }
}
