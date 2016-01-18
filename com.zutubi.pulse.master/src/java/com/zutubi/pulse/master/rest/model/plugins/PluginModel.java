package com.zutubi.pulse.master.rest.model.plugins;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginDependency;
import com.zutubi.util.EnumUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Models a plugin for the API.
 */
public class PluginModel
{
    private Plugin plugin;

    public PluginModel(Plugin plugin)
    {
        this.plugin = plugin;
    }

    public String getId()
    {
        return plugin.getId();
    }

    public String getName()
    {
        return plugin.getName();
    }

    public String getDescription()
    {
        return plugin.getDescription();
    }

    public String getVendor()
    {
        return plugin.getVendor();
    }

    public String getVersion()
    {
        return plugin.getVersion().toString();
    }

    public String getState()
    {
        return EnumUtils.toPrettyString(plugin.getState());
    }

    public List<String> getErrorMessages()
    {
        List<String> messages = plugin.getErrorMessages();
        if (messages.isEmpty())
        {
            return null;
        }
        else
        {
            return messages;
        }
    }

    public List<PluginHandleModel> getRequirements()
    {
        List<PluginDependency> requiredPlugins = plugin.getRequiredPlugins();
        if (requiredPlugins.isEmpty())
        {
            return null;
        }
        else
        {
            return Ordering.from(new PluginHandleModelComparator()).sortedCopy(Lists.transform(requiredPlugins, new Function<PluginDependency, PluginHandleModel>()
            {
                @Override
                public PluginHandleModel apply(PluginDependency input)
                {
                    return new PluginHandleModel(input);
                }
            }));
        }
    }

    public List<PluginHandleModel> getDependents()
    {
        List<Plugin> dependentPlugins = plugin.getDependentPlugins();
        if (dependentPlugins.isEmpty())
        {
            return null;
        }
        else
        {
            return Ordering.from(new PluginHandleModelComparator()).sortedCopy(Lists.transform(dependentPlugins, new Function<Plugin, PluginHandleModel>()
            {
                @Override
                public PluginHandleModel apply(Plugin input)
                {
                    return new PluginHandleModel(input);
                }
            }));
        }
    }

    public List<String> getActions()
    {
        List<String> actions = new ArrayList<>();
        if (plugin.canEnable())
        {
            actions.add("enable");
        }

        if (plugin.canDisable())
        {
            actions.add("disable");
        }

        if (plugin.canUninstall())
        {
            actions.add("uninstall");
        }

        if (actions.isEmpty())
        {
            return null;
        }
        else
        {
            return actions;
        }
    }

    private static class PluginHandleModelComparator implements Comparator<PluginHandleModel>
    {
        @Override
        public int compare(PluginHandleModel o1, PluginHandleModel o2)
        {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
