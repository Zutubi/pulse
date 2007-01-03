package com.zutubi.pulse.web.admin.plugins;

import com.zutubi.pulse.plugins.Plugin;

/**
 * <class comment/>
 */
public class EnablePluginAction extends PluginActionSupport
{
    private String id;

    public void setId(String id)
    {
        this.id = id;
    }

    public String execute() throws Exception
    {
        Plugin plugin = pluginManager.getPlugin(id);
        pluginManager.enablePlugin(plugin);
        
        return SUCCESS;
    }
}
