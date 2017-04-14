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

package com.zutubi.pulse.master;

import com.zutubi.pulse.core.ResourceRepositorySupport;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * A resource repository based on the master's global settings.
 */
public class MasterResourceRepository extends ResourceRepositorySupport
{
    private ConfigurationProvider configurationProvider;

    public ResourceConfiguration getResource(String name)
    {
        GlobalConfiguration globalConfiguration = configurationProvider.get(GlobalConfiguration.class);
        if (globalConfiguration == null)
        {
            return null;
        }
        else
        {
            return globalConfiguration.getResources().get(name);
        }
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
