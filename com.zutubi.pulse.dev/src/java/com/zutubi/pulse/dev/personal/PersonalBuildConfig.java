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

package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.pulse.dev.config.DevConfig;
import com.zutubi.util.config.Config;

import java.io.File;
import java.util.Properties;

/**
 * Extends {@link com.zutubi.pulse.dev.config.DevConfig} with personal build
 * specific properties and convenience accessors for them.
 */
public class PersonalBuildConfig extends DevConfig
{
    public static final String PROPERTY_PROJECT = "project";

    public static final String PROPERTY_SYNC_PLUGINS = "sync.plugins";
    
    public static final String PROPERTY_CHECK_REPOSITORY = "check.repository";

    public static final String PROPERTY_REVISION = "revision";
    public static final String PROPERTY_REASON = "reason";
    public static final String PROPERTY_UPDATE = "update";
    public static final String PROPERTY_PATCH_FILE = "patch.file";
    public static final String PROPERTY_PATCH_TYPE = "patch.format";
    public static final String PROPERTY_SEND_REQUEST = "send.request";

    private Properties overrides;

    public PersonalBuildConfig(File base, UserInterface ui, String... files)
    {
        this(base, null, new Properties(), ui, files);
    }

    public PersonalBuildConfig(File base, Config uiConfig, Properties overrides, UserInterface ui, String... files)
    {
        super(base, uiConfig, ui, files);
        this.overrides = overrides;
    }

    public Properties getOverrides()
    {
        return overrides;
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

    public Boolean getSyncPlugins()
    {
        return getBooleanProperty(PROPERTY_SYNC_PLUGINS, null);
    }

    public boolean setSyncPlugins(boolean sync)
    {
        return setBooleanProperty(PROPERTY_SYNC_PLUGINS, sync);
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

    public String getReason()
    {
        return getProperty(PROPERTY_REASON);
    }

    public void setReason(String reason)
    {
        setProperty(PROPERTY_REASON, reason);
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
