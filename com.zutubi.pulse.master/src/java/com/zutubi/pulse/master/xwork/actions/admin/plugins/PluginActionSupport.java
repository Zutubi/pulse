package com.zutubi.pulse.master.xwork.actions.admin.plugins;

import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

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
