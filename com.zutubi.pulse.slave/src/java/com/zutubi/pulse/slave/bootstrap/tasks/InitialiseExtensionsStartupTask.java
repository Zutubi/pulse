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

import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

/**
 * A startup task that instructs the plugin manager to initialise extension
 * points.
 */
public class InitialiseExtensionsStartupTask implements StartupTask
{
    public void execute()
    {
        PluginManager pluginManager = SpringComponentContext.getBean("pluginManager");
        pluginManager.initialiseExtensions();
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
