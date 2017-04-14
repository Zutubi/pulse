/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        if (requiredPlugins == null || requiredPlugins.isEmpty())
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
        if (dependentPlugins == null || dependentPlugins.isEmpty())
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
