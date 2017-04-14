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

package com.zutubi.pulse.master.tove.config.project.hooks;

import java.util.List;

/**
 * Actions for automatic build hooks.
 */
public class AutoBuildHookConfigurationActions extends BuildHookConfigurationActions
{
    private static final String ACTION_DISABLE = "disable";
    private static final String ACTION_ENABLE  = "enable";

    public List<String> getActions(AutoBuildHookConfiguration instance)
    {
        List<String> actions = super.getActions(instance);

        if(instance.isEnabled())
        {
            actions.add(ACTION_DISABLE);
        }
        else
        {
            actions.add(ACTION_ENABLE);
        }

        return actions;
    }

    public void doEnable(AutoBuildHookConfiguration instance)
    {
        setInstanceEnabled(instance, true);
    }

    public void doDisable(AutoBuildHookConfiguration instance)
    {
        setInstanceEnabled(instance, false);
    }

    private void setInstanceEnabled(AutoBuildHookConfiguration instance, boolean enabled)
    {
        instance = configurationProvider.deepClone(instance);
        instance.setEnabled(enabled);
        configurationProvider.save(instance);
    }
}
