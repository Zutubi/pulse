package com.zutubi.pulse.web.admin.plugins;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.plugins.PluginManager;

/**
 * <class comment/>
 */
public class PluginActionSupport extends ActionSupport
{
    protected PluginManager pluginManager;

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
