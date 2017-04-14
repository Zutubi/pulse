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

package com.zutubi.pulse.dev.bootstrap;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.command.PulseCtl;
import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;

/**
 * Default implementation of DevPaths: used in a real Pulse instance.
 */
public class DefaultDevPaths implements DevPaths
{
    private File versionHome;
    private File userRoot;
    private File systemRoot;
    private File systemPluginRoot;
    private File pluginConfigurationRoot;
    private File internalPluginRoot;
    private File prepackagedPluginRoot;
    private File userPluginRoot;
    private File pluginWorkRoot;

    public DefaultDevPaths()
    {
        String v = System.getProperty(PulseCtl.VERSION_HOME);
        versionHome = new File(v);
    }

    public DefaultDevPaths(File userRoot, File versionHome)
    {
        this.userRoot = userRoot;
        this.versionHome = versionHome;
    }
    
    public File getUserRoot()
    {
        if (userRoot == null)
        {
            File homeDir;
            String userHome = System.getProperty(EnvConfig.USER_HOME);
            if(userHome == null)
            {
                homeDir = new File(".");
            }
            else
            {
                homeDir = new File(userHome);
            }

            userRoot = new File(homeDir, ".pulse" + getMajorMinor() + "-dev");
        }

        return userRoot;
    }

    private String getMajorMinor()
    {
        String versionNumber = Version.getVersion().getVersionNumber();
        String[] pieces = versionNumber.split("\\.");
        if (pieces.length == 1)
        {
            return pieces[0];
        }
        else
        {
            return pieces[0] + pieces[1];
        }
    }

    private File getSystemRoot()
    {
        if(systemRoot == null)
        {
            systemRoot = new File(versionHome, "system");
        }
        return systemRoot;
    }

    private File getSystemPluginRoot()
    {
        if(systemPluginRoot == null)
        {
            systemPluginRoot = new File(getSystemRoot(), "plugins");
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
            userPluginRoot = new File(getUserRoot(), "plugins");
        }
        return userPluginRoot;
    }

    public File getPluginRegistryDir()
    {
        return getPluginStorageDir();
    }

    public File getPluginWorkDir()
    {
        if(pluginWorkRoot == null)
        {
            pluginWorkRoot = FileSystemUtils.getSystemTempDir();
            if(!pluginWorkRoot.isDirectory())
            {
                pluginWorkRoot.mkdirs();
            }
        }

        return pluginWorkRoot;
    }
}
