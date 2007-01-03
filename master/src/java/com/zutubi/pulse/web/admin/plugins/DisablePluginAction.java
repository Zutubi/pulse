package com.zutubi.pulse.web.admin.plugins;

import com.zutubi.pulse.plugins.Plugin;

/**
 * <class comment/>
 */
public class DisablePluginAction extends PluginActionSupport
{
    private String name;

    public void setName(String name)
    {
        this.name = name;
    }

    public String execute() throws Exception
    {
        Plugin plugin = pluginManager.getPlugin(name);
        pluginManager.disablePlugin(plugin);

        return SUCCESS;
    }
}
