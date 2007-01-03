package com.zutubi.pulse.web.admin.plugins;

import com.zutubi.pulse.plugins.Plugin;

import java.util.List;

/**
 * <class comment/>
 */
public class PluginsAction extends PluginActionSupport
{
    private List<Plugin> plugins;

    public List<Plugin> getPlugins()
    {
        return plugins;
    }

    public String execute() throws Exception
    {
        plugins = pluginManager.getAllPlugins();
        return SUCCESS;
    }
}
