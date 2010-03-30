package com.zutubi.pulse.master.xwork.actions.admin.plugins;

import com.zutubi.pulse.core.plugins.Plugin;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An action showing for the administration > plugins page.
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
        plugins = pluginManager.getPlugins();

        // sort plugins for display
        final Collator collator = Collator.getInstance();
        Collections.sort(plugins, new Comparator<Plugin>()
        {
            public int compare(Plugin o1, Plugin o2)
            {
                return collator.compare(o1.getName(), o2.getName());
            }
        });

        return SUCCESS;
    }
}
