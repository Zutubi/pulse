package com.zutubi.pulse.master.xwork.actions.admin.plugins;

import com.zutubi.pulse.core.plugins.Plugin;

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
            plugin.uninstall();
        }

        return SUCCESS;
    }
}
