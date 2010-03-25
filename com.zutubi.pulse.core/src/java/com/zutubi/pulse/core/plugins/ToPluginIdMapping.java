package com.zutubi.pulse.core.plugins;

import com.zutubi.util.Mapping;

/**
 * A mapping that maps a plugin to its id.
 */
public class ToPluginIdMapping implements Mapping<Plugin, String>
{
    public String map(Plugin plugin)
    {
        return plugin.getId();
    }
}
