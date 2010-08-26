package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.pulse.dev.config.DevConfig;
import com.zutubi.util.config.Config;

import java.io.File;

/**
 * Extends {@link com.zutubi.pulse.dev.config.DevConfig} with personal build
 * specific properties and convenience accessors for them.
 */
public class PersonalBuildConfig extends DevConfig
{
    public static final String PROPERTY_PROJECT = "project";

    public static final String PROPERTY_CHECK_REPOSITORY = "check.repository";

    public static final String PROPERTY_REVISION = "revision";
    public static final String PROPERTY_UPDATE = "update";
    public static final String PROPERTY_PATCH_FILE = "patch.file";
    public static final String PROPERTY_PATCH_TYPE = "patch.format";
    public static final String PROPERTY_SEND_REQUEST = "send.request";

    public PersonalBuildConfig(File base, UserInterface ui, String... files)
    {
        this(base, null, ui, files);
    }

    public PersonalBuildConfig(File base, Config uiConfig, UserInterface ui, String... files)
    {
        super(base, uiConfig, ui, files);
    }
    public String getProject()
    {
        return getProperty(PROPERTY_PROJECT);
    }

    public boolean getCheckRepository()
    {
        return getBooleanProperty(PROPERTY_CHECK_REPOSITORY, true);
    }

    public boolean setCheckRepository(boolean check)
    {
        return setBooleanProperty(PROPERTY_CHECK_REPOSITORY, check);
    }

    public Boolean getUpdate()
    {
        return getBooleanProperty(PROPERTY_UPDATE, null);
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
        return getBooleanProperty(PROPERTY_SEND_REQUEST, true);
    }

    public boolean setSendRequest(boolean sendRequest)
    {
        return setBooleanProperty(PROPERTY_SEND_REQUEST, sendRequest);
    }
}
