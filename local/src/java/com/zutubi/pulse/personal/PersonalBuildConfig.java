package com.zutubi.pulse.personal;

import com.zutubi.pulse.config.*;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;

/**
 */
public class PersonalBuildConfig implements Config
{
    private static final String PROPERTIES_FILENAME = ".pulse.properties";
    
    public static final String PROPERTY_PULSE_URL = "pulse.url";
    public static final String PROPERTY_PULSE_USER = "pulse.user";
    public static final String PROPERTY_PULSE_PASSWORD = "pulse.password";
    public static final String PROPERTY_PROXY_HOST = "proxy.host";
    public static final String PROPERTY_PROXY_PORT = "proxy.port";
    public static final String PROPERTY_PROJECT = "project";
    public static final String PROPERTY_SPECIFICATION = "specification";

    public static final String PROPERTY_CHECK_REPOSITORY = "check.repository";
    public static final String PROPERTY_CONFIRM_UPDATE = "confirm.update";
    public static final String PROPERTY_CONFIRMED_VERSION = "confirmed.version";

    private File base;
    private ConfigSupport config;
    private ConfigSupport localConfig;
    private ConfigSupport userConfig;

    public PersonalBuildConfig(File base, Config ui)
    {
        this.base = base;
        
        // First, properties defined by the UI that is invoking us (e.g.
        // the command line)
        CompositeConfig composite = new CompositeConfig(ui);

        // Next: system properties
        composite.append(new PropertiesConfig(System.getProperties()));

        localConfig = new ConfigSupport(new FileConfig(getLocalConfigFile()));
        composite.append(localConfig);

        // Now all properties files in the parent directories
        base = base.getParentFile();
        while(base != null)
        {
            File properties = new File(base, PROPERTIES_FILENAME);
            if(properties.isFile())
            {
                composite.append(new FileConfig(properties));
            }
            base = base.getParentFile();
        }

        // Next: user's settings from properties in home directory
        File userFile = getUserConfigFile();
        if(userFile != null)
        {
            userConfig = new ConfigSupport(new FileConfig(userFile));
            composite.append(userConfig);
        }

        // Lowest priority: defaults
        composite.append(getDefaults());

        config = new ConfigSupport(composite);
    }

    private Config getDefaults()
    {
        PropertiesConfig defaults = new PropertiesConfig();

        String userName = System.getProperty("user.name");
        if(userName != null)
        {
            defaults.setProperty(PROPERTY_PULSE_USER, userName);
        }

        defaults.setProperty(PROPERTY_SPECIFICATION, "");

        return defaults;
    }

    public String getPulseUrl()
    {
        String url = config.getProperty(PROPERTY_PULSE_URL);
        if(url != null && url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public String getPulseUser()
    {
        return config.getProperty(PROPERTY_PULSE_USER);
    }

    public String getPulsePassword()
    {
        return config.getProperty(PROPERTY_PULSE_PASSWORD);
    }

    public String getProxyHost()
    {
        return config.getProperty(PROPERTY_PROXY_HOST);
    }

    public Integer getProxyPort()
    {
        return config.getInteger(PROPERTY_PROXY_PORT, 80);
    }

    public String getProject()
    {
        return config.getProperty(PROPERTY_PROJECT);
    }

    public String getSpecification()
    {
        return config.getProperty(PROPERTY_SPECIFICATION);
    }

    public File getBase()
    {
        return base;
    }

    public boolean getCheckRepository()
    {
        return config.getBooleanProperty(PROPERTY_CHECK_REPOSITORY, true);
    }

    public boolean setCheckRepository(boolean check)
    {
        return setBooleanProperty(PROPERTY_CHECK_REPOSITORY, check);
    }

    public boolean getConfirmUpdate()
    {
        return config.getBooleanProperty(PROPERTY_CONFIRM_UPDATE, true);
    }

    public boolean setConfirmUpdate(boolean confirm)
    {
        return setBooleanProperty(PROPERTY_CONFIRM_UPDATE, confirm);
    }

    public int getConfirmedVersion()
    {
        return config.getInteger(PROPERTY_CONFIRMED_VERSION, 0);
    }

    public boolean setConfirmedVersion(int version)
    {
        return setIntegerProperty(PROPERTY_CONFIRMED_VERSION, version);
    }

    private boolean setBooleanProperty(String property, boolean value)
    {
        if(userConfig == null)
        {
            return false;
        }
        else
        {
            userConfig.setBooleanProperty(property, value);
            return true;
        }
    }

    private boolean setIntegerProperty(String property, int value)
    {
        if(userConfig == null)
        {
            return false;
        }
        else
        {
            userConfig.setInteger(property, value);
            return true;
        }
    }

    public void setProperty(String key, String value)
    {
        setProperty(key, value, false);
    }

    public void setProperty(String key, String value, boolean local)
    {
        if (local)
        {
            localConfig.setProperty(key, value);
        }
        else
        {
            userConfig.setProperty(key, value);
        }
    }


    public void removeProperty(String key)
    {
        config.removeProperty(key);
    }

    public boolean hasProperty(String key)
    {
        return config.hasProperty(key);
    }

    public String getProperty(String key)
    {
        return config.getProperty(key);
    }

    public boolean isWriteable()
    {
        return config.isWriteable();
    }

    public File getUserConfigFile()
    {
        String userHome = System.getProperty("user.home");
        if(userHome != null)
        {
            return FileSystemUtils.composeFile(userHome, PROPERTIES_FILENAME);
        }

        return null;
    }

    public File getLocalConfigFile()
    {
        return new File(base, PROPERTIES_FILENAME);
    }
}
