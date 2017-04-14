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

package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.master.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 */
public class SystemStartedMessageStartupTask implements StartupTask
{
    private MasterConfigurationManager configurationManager;
    private ConfigurationProvider configurationProvider;

    public void execute()
    {
        // let the user know that the system is now up and running.
        GlobalConfiguration globalConfig = configurationProvider.get(GlobalConfiguration.class);
        SystemConfiguration sysConfig = configurationManager.getSystemConfig();

        //TODO: I18N this message.
        System.err.printf(DefaultSetupManager.dateStamp() + "Pulse %s is now available on port %s at context path '%s' [base URL configured as: %s]\n", Version.getVersion().getVersionNumber(), sysConfig.getServerPort(), sysConfig.getContextPath(), globalConfig.getBaseUrl());
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
