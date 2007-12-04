package com.zutubi.pulse.web.admin.plugins;

import com.zutubi.pulse.plugins.Plugin;

import java.util.List;

/**
 An action showing for the administration > plugins page.
 */
public class PluginsAction extends PluginActionSupport
{
    private List<Plugin> plugins;
    /**
     * Path of the currently-selected tree node.
     */
    private String path;

    public List<Plugin> getPlugins()
    {
        return plugins;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String execute() throws Exception
    {
        plugins = pluginManager.getNonInternalPlugins();
        return SUCCESS;
    }
}
