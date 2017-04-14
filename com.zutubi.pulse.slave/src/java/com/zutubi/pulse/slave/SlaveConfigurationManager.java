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

package com.zutubi.pulse.slave;

import com.zutubi.pulse.servercore.bootstrap.AbstractConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;

/**
 */
public class SlaveConfigurationManager extends AbstractConfigurationManager
{
    private DefaultSlaveConfiguration appConfig;
    private SlaveUserPaths userPaths;

    public SlaveConfigurationManager()
    {
    }

    public void init()
    {
        SystemPaths systemPaths = getSystemPaths();
        appConfig = new DefaultSlaveConfiguration(systemPaths, getEnvConfig());
        userPaths = new SlaveUserPaths(appConfig);
    }

    public SystemConfiguration getSystemConfig()
    {
        return appConfig;
    }

    public SlaveConfiguration getAppConfig()
    {
        return appConfig;
    }

    public SlaveUserPaths getUserPaths()
    {
        return userPaths;
    }
}
