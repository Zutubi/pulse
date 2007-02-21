package com.zutubi.pulse.web.admin.plugins;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.web.LookupErrorException;
import com.zutubi.pulse.plugins.Plugin;
import com.zutubi.pulse.plugins.PluginRequirement;

import java.util.List;

/**
 */
public class ViewPluginAction extends PluginActionSupport
{
    private String id;
    private Plugin plugin;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Plugin getPlugin()
    {
        return plugin;
    }

    public List<PluginRequirement> getRequiredPlugins()
    {
        return pluginManager.getRequiredPlugins(plugin);
    }

    public List<Plugin> getDependentPlugins()
    {
        return pluginManager.getDependentPlugins(plugin);
    }

    public String execute() throws Exception
    {
        plugin = pluginManager.getPlugin(id);
        if(plugin == null)
        {
            throw new LookupErrorException("Unknown plugin [" + id + "]");
        }
        
        return SUCCESS;
    }
}
