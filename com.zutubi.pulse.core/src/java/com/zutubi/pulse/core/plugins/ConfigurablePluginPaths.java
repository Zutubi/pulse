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

package com.zutubi.pulse.core.plugins;

import java.io.File;

/**
 *
 *
 */
public class ConfigurablePluginPaths implements PluginPaths
{
    private File internalPluginStorageDir;
    private File pluginStorageDir;
    private File pluginWorkDir;
    private File pluginRegistryDir;
    private File osgiConfigurationDir;
    private File prepackagedPluginStorageDir;

    /**
     * The internal plugin storage directory is the location of pulses' internal plugins, those that come
     * bundled with pulse. 
     *
     * @return
     */
    public File getInternalPluginStorageDir()
    {
        return internalPluginStorageDir;
    }

    public void setInternalPluginStorageString(String internalPluginStorageDir)
    {
        this.internalPluginStorageDir = new File(internalPluginStorageDir);
    }

    public void setInternalPluginStorageDir(File internalPluginStorageDir)
    {
        this.internalPluginStorageDir = internalPluginStorageDir;
    }

    /**
     * The plugin storage directory is the directory in which all but the internal plugins are stored.  This
     * includes pre-packaged plugins and manually installed plugins.
     *
     * @return
     */
    public File getPluginStorageDir()
    {
        return pluginStorageDir;
    }

    public void setPluginStorageString(String pluginStorageDir)
    {
        this.pluginStorageDir = new File(pluginStorageDir);
    }

    public void setPluginStorageDir(File pluginStorageDir)
    {
        this.pluginStorageDir = pluginStorageDir;
    }

    /**
     * The plugin work directory is a scratch directory into which temporary files can be written.  For example,
     * when a plugin is upgraded, the upgrade will be downloaded into the working directory, and installed on restart.
     *
     * @return
     */
    public File getPluginWorkDir()
    {
        return pluginWorkDir;
    }

    public void setPluginWorkString(String pluginWorkDir)
    {
        this.pluginWorkDir = new File(pluginWorkDir);
    }

    public void setPluginWorkDir(File pluginWorkDir)
    {
        this.pluginWorkDir = pluginWorkDir;
    }

    /**
     * The plugin registry directory is the directory in which the plugin registry is stored.
     *
     * @return
     */
    public File getPluginRegistryDir()
    {
        return pluginRegistryDir;
    }

    public void setPluginRegistryString(String pluginRegistryDir)
    {
        this.pluginRegistryDir = new File(pluginRegistryDir);
    }

    public void setPluginRegistryDir(File pluginRegistryDir)
    {
        this.pluginRegistryDir = pluginRegistryDir;
    }

    public File getOsgiConfigurationDir()
    {
        return osgiConfigurationDir;
    }

    public void setOsgiConfigurationString(String osgiConfigurationDir)
    {
        this.osgiConfigurationDir = new File(osgiConfigurationDir);
    }

    public void setOsgiConfigurationDir(File osgiConfigurationDir)
    {
        this.osgiConfigurationDir = osgiConfigurationDir;
    }

    public File getPrepackagedPluginStorageDir()
    {
        return prepackagedPluginStorageDir;
    }

    public void setPrepackagedPluginStorageString(String prepackagedPluginStorageDir)
    {
        this.prepackagedPluginStorageDir = new File(prepackagedPluginStorageDir);
    }

    public void setPrepackagedPluginStorageDir(File prepackagedPluginStorageDir)
    {
        this.prepackagedPluginStorageDir = prepackagedPluginStorageDir;
    }
}
