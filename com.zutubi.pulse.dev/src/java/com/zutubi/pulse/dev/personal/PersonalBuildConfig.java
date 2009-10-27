package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.scm.api.PersonalBuildUI;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Sort;
import com.zutubi.util.config.*;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for the personal build command.  Extends the regular
 * {@link com.zutubi.util.config.Config} interface with support for loading
 * .pulse2.properties files and convenience methods for accessing the core
 * personal build properties.
 * <p/>
 * The .properties files are loaded from the personal build base directory,
 * then its parent directory and so on.  A .properties file in the user's
 * home directory is also added to the end of this chain if it exists.
 * <p/>
 * Configuration provided from an external user interface may be passed in to
 * the constrtcutor to take precedence over the .properties files.
 */
public class PersonalBuildConfig implements Config
{
    public static final String PROPERTIES_FILENAME = ".pulse2.properties";
    
    public static final String PROPERTY_PULSE_URL = "pulse.url";
    public static final String PROPERTY_PULSE_USER = "pulse.user";
    public static final String PROPERTY_PULSE_PASSWORD = "pulse.password";
    public static final String PROPERTY_PROXY_HOST = "proxy.host";
    public static final String PROPERTY_PROXY_PORT = "proxy.port";
    public static final String PROPERTY_PROJECT = "project";

    public static final String PROPERTY_CHECK_REPOSITORY = "check.repository";
    public static final String PROPERTY_CONFIRMED_VERSION = "confirmed.version";

    public static final String PROPERTY_REVISION = "revision";
    public static final String PROPERTY_UPDATE = "update";
    public static final String PROPERTY_PATCH_FILE = "patch.file";
    public static final String PROPERTY_PATCH_TYPE = "patch.format";
    public static final String PROPERTY_SEND_REQUEST = "send.request";

    private File base;
    private String[] files;
    private ConfigSupport config;
    private ConfigSupport localConfig;
    private ConfigSupport userConfig;

    public PersonalBuildConfig(File base, PersonalBuildUI ui, String... files)
    {
        this(base, null, ui, files);
    }

    public PersonalBuildConfig(File base, Config uiConfig, PersonalBuildUI ui, String... files)
    {
        this.base = base;
        this.files = files;
        CompositeConfig composite = new CompositeConfig();

        ui.debug("Assembling configuration...");
        ui.enterContext();
        
        // First, properties defined by the UI that is invoking us (e.g.
        // the command line)
        if (uiConfig != null)
        {
            ui.debug("UI configuration.");
            composite.append(uiConfig);
        }

        // Next: system properties
        ui.debug("System properties.");
        composite.append(new PropertiesConfig(System.getProperties()));

        File localConfigFile = getLocalConfigFile();
        FileConfig fileConfig = new FileConfig(localConfigFile);
        showFileConfig(ui, localConfigFile, fileConfig);
        this.localConfig = new ConfigSupport(fileConfig);
        composite.append(this.localConfig);

        // Now all properties files in the parent directories
        base = base.getParentFile();
        while(base != null)
        {
            File properties = new File(base, PROPERTIES_FILENAME);
            if(properties.isFile())
            {
                fileConfig = new FileConfig(properties);
                showFileConfig(ui, properties, fileConfig);
                composite.append(fileConfig);
            }
            base = base.getParentFile();
        }

        // Next: user's settings from properties in home directory
        File userFile = getUserConfigFile();
        if(userFile != null)
        {
            fileConfig = new FileConfig(userFile);
            userConfig = new ConfigSupport(fileConfig);
            composite.append(userConfig);
            showFileConfig(ui, userFile, fileConfig);
        }

        // Lowest priority: defaults
        ui.debug("Defaults.");
        composite.append(getDefaults());

        config = new ConfigSupport(composite);
        ui.exitContext();
        ui.debug("Configuration assembled.");
    }

    private void showFileConfig(PersonalBuildUI ui, File file, FileConfig config)
    {
        if (ui.isDebugEnabled() && file.isFile())
        {
            ui.debug("File '" + file.getAbsolutePath() + "'.");
            ui.enterContext();

            List<String> properties = new LinkedList<String>();
            for (Object key: config.getProperties().keySet())
            {
                properties.add((String) key);
            }
            
            Collections.sort(properties, new Sort.StringComparator());
            for (String property: properties)
            {
                String value = property.contains("password") ? "[suppressed]" : config.getProperty(property);
                ui.debug(property + " -> " + value);
            }

            ui.exitContext();
        }
    }

    public File getBase()
    {
        return base;
    }

    public String[] getFiles()
    {
        return files;
    }

    private Config getDefaults()
    {
        PropertiesConfig defaults = new PropertiesConfig();

        String userName = System.getProperty("user.name");
        if (userName != null)
        {
            defaults.setProperty(PROPERTY_PULSE_USER, userName);
        }

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

    public boolean getCheckRepository()
    {
        return config.getBooleanProperty(PROPERTY_CHECK_REPOSITORY, true);
    }

    public boolean setCheckRepository(boolean check)
    {
        return setBooleanProperty(PROPERTY_CHECK_REPOSITORY, check);
    }

    public int getConfirmedVersion()
    {
        return config.getInteger(PROPERTY_CONFIRMED_VERSION, 0);
    }

    public boolean setConfirmedVersion(int version)
    {
        return setIntegerProperty(PROPERTY_CONFIRMED_VERSION, version);
    }

    public Boolean getUpdate()
    {
        return config.getBooleanProperty(PROPERTY_UPDATE, null);
    }

    public boolean setUpdate(boolean update)
    {
        return setBooleanProperty(PROPERTY_UPDATE, update);
    }

    public String getRevision()
    {
        return getProperty(PROPERTY_REVISION);
    }

    public void setRevision(String revision)
    {
        setProperty(PROPERTY_REVISION, revision);
    }

    public String getPatchFile()
    {
        return getProperty(PROPERTY_PATCH_FILE);
    }

    public void setPatchFile(String patchFile)
    {
        setProperty(PROPERTY_PATCH_FILE, patchFile);
    }

    public String getPatchType()
    {
        return getProperty(PROPERTY_PATCH_TYPE);
    }

    public void setPatchType(String patchType)
    {
        setProperty(PROPERTY_PATCH_TYPE, patchType);
    }

    public boolean getSendRequest()
    {
        return config.getBooleanProperty(PROPERTY_SEND_REQUEST, true);
    }

    public boolean setSendRequest(boolean sendRequest)
    {
        return setBooleanProperty(PROPERTY_SEND_REQUEST, sendRequest);
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
        if (userHome != null)
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
