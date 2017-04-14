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

package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 */
public class DefaultMasterLocationProvider implements MasterLocationProvider
{
    private ConfigurationProvider configurationProvider;
    private SystemConfiguration systemConfiguration;

    public String getMasterLocation()
    {
        String url = configurationProvider.get(GlobalConfiguration.class).getMasterHost() + ":" + systemConfiguration.getServerPort() + systemConfiguration.getContextPath();
        if(url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }

    public String getMasterUrl()
    {
        String protocol = "http://";
        if(systemConfiguration.isSslEnabled())
        {
            protocol = "https://";
        }
        
        return protocol + getMasterLocation();
    }

    public void setSystemConfiguration(SystemConfiguration systemConfiguration)
    {
        this.systemConfiguration = systemConfiguration;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
