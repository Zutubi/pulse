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

package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.notifications.NotifyConditionContext;
import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * A subscription to results for personal builds.
 */
@SymbolicName("zutubi.personalSubscriptionConfig")
@Form(fieldOrder = {"name", "contact", "template", "attachLogs", "logLineLimit"})
@Classification(single = "favourite")
@Wire
public class PersonalSubscriptionConfiguration extends SubscriptionConfiguration
{
    private ConfigurationProvider configurationProvider;

    public boolean conditionSatisfied(NotifyConditionContext context)
    {
        BuildResult buildResult = context.getBuildResult();
        return buildResult.isPersonal() && buildResult.getUser().getConfig().equals(configurationProvider.getAncestorOfType(this, UserConfiguration.class));
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
