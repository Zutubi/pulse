package com.zutubi.pulse.web.admin.plugins;

import com.zutubi.pulse.core.plugins.LocalPlugin;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginDependency;
import com.zutubi.pulse.web.LookupErrorException;

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

    public List<PluginDependency> getRequiredPlugins()
    {
        // this only works for installed plugins.
        LocalPlugin installedPlugin = (LocalPlugin) plugin;
        return installedPlugin.getRequiredPlugins();
    }

    public List<Plugin> getDependentPlugins()
    {
        // this only works for installed plugins.
        LocalPlugin installedPlugin = (LocalPlugin) plugin;
        return installedPlugin.getDependentPlugins();
    }

    public String execute() throws Exception
    {
        plugin = pluginManager.getPlugin(id);
        if (plugin == null)
        {
            throw new LookupErrorException("Unknown plugin [" + id + "]");
        }

        return SUCCESS;
    }
}
