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

import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.util.StringUtils;
import com.zutubi.util.config.PropertiesConfig;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 */
public abstract class AbstractConfigurationManager implements ConfigurationManager
{
    private static final Logger LOG = Logger.getLogger(AbstractConfigurationManager.class);

    private static final String PROPERTY_DISK_SPACE_PATH = "pulse.disk.space.path";

    private SystemPaths systemPaths = null;
    protected EnvConfig envConfig;

    /**
     * The Environment Config provides read only access to important configuration properties
     * that are defined in the startup environment.
     */
    public EnvConfig getEnvConfig()
    {
        if (envConfig == null)
        {
            envConfig = new EnvConfig(new PropertiesConfig(System.getProperties()));
        }
        return envConfig;
    }

    public SystemPaths getSystemPaths()
    {
        if (systemPaths == null)
        {
            File pulseHome = getHomeDir(getEnvConfig().getPulseHome(), EnvConfig.PULSE_HOME);
            File versionHome = getHomeDir(getEnvConfig().getVersionHome(), EnvConfig.VERSION_HOME);

            // initialise system paths based on pulse.home.
            systemPaths = new DefaultSystemPaths(pulseHome, versionHome);
        }
        return systemPaths;
    }

    public File getDiskSpacePath()
    {
        String path = System.getProperty(PROPERTY_DISK_SPACE_PATH);
        if (StringUtils.stringSet(path))
        {
            return new File(path);
        }
        else
        {
            return getUserPaths().getData();
        }
    }

    public Map<String, String> getCoreProperties()
    {
        Map<String, String> result = new LinkedHashMap<String, String>();

        EnvConfig envConfig = getEnvConfig();
        String pulseHome = envConfig.getPulseHome();
        if(pulseHome != null)
        {
            result.put(CORE_PROPERTY_PULSE_HOME_DIR, getHomeDir(pulseHome, EnvConfig.PULSE_HOME).getAbsolutePath());
        }

        SystemConfiguration systemConfig = getSystemConfig();

        result.put(CORE_PROPERTY_USER_HOME_DIR, envConfig.getUserHome());
        result.put(CORE_PROPERTY_PULSE_CONFIG_FILE, systemConfig.getConfigFilePath());
        result.put(CORE_PROPERTY_PULSE_DATA_DIR, getUserPaths().getData().getAbsolutePath());
        result.put(CORE_PROPERTY_PULSE_BIND_ADDRESS, systemConfig.getBindAddress());
        result.put(CORE_PROPERTY_PULSE_WEBAPP_PORT, Integer.toString(systemConfig.getServerPort()));
        result.put(CORE_PROPERTY_PULSE_CONTEXT_PATH, systemConfig.getContextPath());
        return result;
    }

    @Override
    public void loadSystemProperties()
    {
        File propFile = new File(getUserPaths().getUserConfigRoot(), "system.properties");
        if (propFile.exists())
        {
            FileInputStream is = null;
            try
            {
                is = new FileInputStream(propFile);
                System.getProperties().load(is);
            }
            catch (IOException e)
            {
                LOG.warning("Unable to load system properties: " + e.getMessage(), e);
            }
            finally
            {
                IOUtils.close(is);
            }
        }

    }

    private File getHomeDir(String home, String property)
    {
        if (home == null || home.length() == 0)
        {
            // fatal error, PULSE_HOME property needs to exist.
            throw new StartupException("Required property '" + property + "' is not set");
        }

        File homeDir = new File(home);
        if (!homeDir.exists() || !homeDir.isDirectory())
        {
            // fatal error, PULSE_HOME property needs to reference pulse's home directory
            throw new StartupException("Property '" + property + "' does not refer to a " +
                    "directory ('" + home + ")");
        }

        try
        {
            homeDir = homeDir.getCanonicalFile();
        }
        catch (IOException e)
        {
            // Not fatal, carry on.
        }

        return homeDir;
    }

    public void setDevelopmentSystemPaths(SystemPaths paths)
    {
        this.systemPaths = paths;
    }
}
