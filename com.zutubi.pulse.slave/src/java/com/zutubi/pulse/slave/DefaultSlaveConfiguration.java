package com.zutubi.pulse.slave;

import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.config.*;

import java.io.File;

/**
 */
public class DefaultSlaveConfiguration implements SlaveConfiguration, SystemConfiguration
{
    private static final String PROPERTIES_FILE = "pulse-agent.properties";
    private static final String CONFIG_DIR = ".pulse2-agent";
    private static final String LOGGING_CONFIG = "log.config";
    private static final String LOG_EVENTS = "log.events";

    private EnvConfig envConfig;
    private String pulseConfigProperties;
    private ConfigSupport config;
    private SystemPaths systemPaths;

    public DefaultSlaveConfiguration(SystemPaths systemPaths, EnvConfig env)
    {
        this.systemPaths = systemPaths;
        init(env);
    }

    public void init(EnvConfig env)
    {
        this.envConfig = env;

        Config commandLineAndSystemProperties = new VolatileReadOnlyConfig(System.getProperties());

        File systemConfigProperties = new File(systemPaths.getConfigRoot(), PROPERTIES_FILE);
        FileConfig systemConfig = new FileConfig(systemConfigProperties);

        pulseConfigProperties = env.getDefaultPulseConfig(CONFIG_DIR);
        if (env.hasPulseConfig())
        {
            pulseConfigProperties = env.getPulseConfig();
        }

        File pulseConfigFile = new File(pulseConfigProperties);
        if(pulseConfigFile.isFile())
        {
            FileConfig userConfig = new FileConfig(pulseConfigFile);
            config = new ConfigSupport(new CompositeConfig(commandLineAndSystemProperties, userConfig, systemConfig));
        }
        else
        {
            config = new ConfigSupport(new CompositeConfig(commandLineAndSystemProperties, systemConfig));
        }
    }

    public String getConfigFilePath()
    {
        return pulseConfigProperties;
    }

    public String getBindAddress()
    {
        String result = config.getProperty(WEBAPP_BIND_ADDRESS);
        if(TextUtils.stringSet(result))
        {
            return result;
        }
        else
        {
            return "0.0.0.0";
        }
    }

    public int getServerPort()
    {
        return config.getInteger(WEBAPP_PORT, 8090);
    }

    public String getLoggingLevel()
    {
        return config.getProperty(LOGGING_CONFIG, "default");
    }

    public String getContextPath()
    {
        return config.getProperty(CONTEXT_PATH, "/");
    }

    public String getRestoreFile()
    {
        return config.getProperty(RESTORE_FILE);
    }

    public String getRestoreArtifacts()
    {
        return config.getProperty(RESTORE_ARTIFACTS);
    }

    public String getContextPathNormalised()
    {
        // TODO share with master.  more of this whole class, preferrably
        String contextPath = getContextPath();
        if(!contextPath.startsWith("/"))
        {
            contextPath = "/" + contextPath;
        }
        if(contextPath.endsWith("/"))
        {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }
        return contextPath;
    }

    public void setDataPath(String path)
    {
        config.setProperty(PULSE_DATA, path);
    }

    public String getDataPath()
    {
        String defaultData = FileSystemUtils.composeFilename(envConfig.getDefaultPulseConfigDir(CONFIG_DIR), "data");
        return config.getProperty(PULSE_DATA, defaultData);
    }

    public boolean isSslEnabled()
    {
        return config.getBooleanProperty(SSL_ENABLED, false);
    }

    public String getSslKeystore()
    {
        return config.getProperty(SSL_KEYSTORE);
    }

    public String getSslPassword()
    {
        return config.getProperty(SSL_PASSWORD);
    }

    public String getSslKeyPassword()
    {
        return config.getProperty(SSL_KEY_PASSWORD);
    }

    public void setLoggingLevel(String c)
    {
        config.setProperty(LOGGING_CONFIG, c);
    }

    public boolean isEventLoggingEnabled()
    {
        return config.getBooleanProperty(LOG_EVENTS, false);
    }
}
