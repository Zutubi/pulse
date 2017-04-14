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

package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.config.ConfigurationProvider;

import java.util.Collections;
import java.util.List;

/**
 * Actions for {@link CommandConfiguration} instances.
 */
public class CommandConfigurationActions
{
    private static final String ACTION_DISABLE = "disable";
    private static final String ACTION_ENABLE  = "enable";

    private ConfigurationProvider configurationProvider;

    public List<String> getActions(CommandConfiguration instance)
    {
        if (instance.isEnabled())
        {
            return Collections.singletonList(ACTION_DISABLE);
        }
        else
        {
            return Collections.singletonList(ACTION_ENABLE);
        }
    }

    public void doDisable(CommandConfiguration instance)
    {
        setInstanceEnabled(instance, false);
    }

    public void doEnable(CommandConfiguration instance)
    {
        setInstanceEnabled(instance, true);
    }

    private void setInstanceEnabled(CommandConfiguration instance, boolean enabled)
    {
        instance = configurationProvider.deepClone(instance);
        instance.setEnabled(enabled);
        configurationProvider.save(instance);
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
