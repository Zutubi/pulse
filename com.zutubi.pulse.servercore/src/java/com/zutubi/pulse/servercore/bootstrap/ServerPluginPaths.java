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

package com.zutubi.pulse.servercore.bootstrap;

import com.zutubi.pulse.core.plugins.PluginPaths;

import java.io.File;

/**
 * An implementation of the plugin paths shared by master and slave.
 */
public class ServerPluginPaths implements PluginPaths
{
    private ConfigurationManager configurationManager;
    private File systemPluginRoot;
    private File pluginConfigurationRoot;
    private File internalPluginRoot;
    private File prepackagedPluginRoot;
    private File userPluginRoot;

    public void init()
    {
    }

    private File getSystemPluginRoot()
    {
        if(systemPluginRoot == null)
        {
            systemPluginRoot = new File(configurationManager.getSystemPaths().getSystemRoot(), "plugins");
        }
        return systemPluginRoot;
    }


    public File getOsgiConfigurationDir()
    {
        if(pluginConfigurationRoot == null)
        {
            pluginConfigurationRoot = new File(getSystemPluginRoot(), "config");
        }
        return pluginConfigurationRoot;
    }

    public File getInternalPluginStorageDir()
    {
        if(internalPluginRoot == null)
        {
            internalPluginRoot = new File(getSystemPluginRoot(), "internal");
        }
        return internalPluginRoot;
    }

    public File getPrepackagedPluginStorageDir()
    {
        if(prepackagedPluginRoot == null)
        {
            prepackagedPluginRoot = new File(getSystemPluginRoot(), "prepackaged");
        }
        return prepackagedPluginRoot;
    }

    public File getPluginStorageDir()
    {
        if(userPluginRoot == null)
        {
            UserPaths userPaths = configurationManager.getUserPaths();
            if (userPaths != null)
            {
                File dataDir = userPaths.getData();
                if (dataDir != null)
                {
                    userPluginRoot = new File(dataDir, "plugins");
                }
            }
        }
        return userPluginRoot;
    }

    public File getPluginRegistryDir()
    {
        return getPluginStorageDir();
    }

    public File getPluginWorkDir()
    {
        return configurationManager.getSystemPaths().getTmpRoot();
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
