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

package com.zutubi.pulse.slave.bootstrap.tasks;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;

import java.text.DateFormat;
import java.util.Date;

/**
 */
public class SlaveStartedMessageStartupTask implements StartupTask
{
    private ConfigurationManager configurationManager;

    public void execute()
    {
        SystemConfiguration sysConfig = configurationManager.getSystemConfig();
        String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(new Date());
        System.err.format("[%s] Pulse agent %s is now listening on port %d\n", date, Version.getVersion().getVersionNumber(), sysConfig.getServerPort());
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
