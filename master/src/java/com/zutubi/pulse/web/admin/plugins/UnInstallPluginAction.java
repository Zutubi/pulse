package com.zutubi.pulse.web.admin.plugins;

import com.zutubi.pulse.plugins.Plugin;

/**
 * <class comment/>
 */
public class UnInstallPluginAction extends PluginActionSupport
{
    private String id;

    public void setId(String id)
    {
        this.id = id;
    }

    public String execute() throws Exception
    {
        Plugin plugin = pluginManager.getPlugin(id);
        if (plugin != null)
        {
            pluginManager.uninstallPlugin(plugin);
        }

        return SUCCESS;
    }
}
