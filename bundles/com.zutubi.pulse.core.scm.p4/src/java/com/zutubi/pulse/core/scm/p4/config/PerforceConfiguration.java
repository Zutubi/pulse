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

package com.zutubi.pulse.core.scm.p4.config;

import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import com.zutubi.pulse.core.scm.p4.PerforceClient;
import com.zutubi.pulse.core.scm.p4.PerforceCore;
import com.zutubi.pulse.core.scm.p4.PerforceWorkspaceManager;
import com.zutubi.tove.annotations.*;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.annotations.Min;
import com.zutubi.validation.annotations.Required;

/**
 * Configures details of a Perforce depot and client.
 */
@Form(fieldOrder = { "port", "user", "password", "useTemplateClient", "spec", "stream", "view", "options", "useTicketAuth", "unicodeServer", "charset", "monitor", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod", "includedPaths", "excludedPaths", "inactivityTimeout", "syncWorkspacePattern", "timeOffset" })
@SymbolicName("zutubi.perforceConfig")
public class PerforceConfiguration extends PollableScmConfiguration
{
    private String port = "perforce:1666";
    private String user;
    @Password
    private String password;
    @ControllingCheckbox(checkedFields = {"spec"}, uncheckedFields = {"options", "stream", "view"})
    private boolean useTemplateClient = true;
    @Required
    private String spec;
    private String options;
    private String stream;
    @TextArea(rows = 10, cols = 64)
    private String view = "//depot/... //pulse/...";
    private boolean useTicketAuth = false;
    @Wizard.Ignore @Min(0)
    private int inactivityTimeout = PerforceCore.DEFAULT_INACTIVITY_TIMEOUT;
    @Wizard.Ignore
    private String syncWorkspacePattern = PerforceWorkspaceManager.getWorkspacePrefix() + "$(project.handle)-$(stage.handle)-$(agent.handle)";
    @Wizard.Ignore
    private int timeOffset= 0;

    @ControllingCheckbox(checkedFields = {"charset"}) @Wizard.Ignore
    private boolean unicodeServer = false;
    @Required @Wizard.Ignore
    private String charset = "none";

    public PerforceConfiguration()
    {
    }

    public PerforceConfiguration(String port, String user, String password, String spec)
    {
        this.port = port;
        this.user = user;
        this.password = password;
        this.spec = spec;
    }

    public String getPort()
    {
        return port;
    }

    public void setPort(String port)
    {
        this.port = port;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean getUseTemplateClient()
    {
        return useTemplateClient;
    }

    public void setUseTemplateClient(boolean useTemplateClient)
    {
        this.useTemplateClient = useTemplateClient;
    }

    public String getSpec()
    {
        return spec;
    }

    public void setSpec(String spec)
    {
        this.spec = spec;
    }

    public String getStream()
    {
        return stream;
    }

    public void setStream(String stream)
    {
        this.stream = stream;
    }

    public String getView()
    {
        return view;
    }

    public void setView(String view)
    {
        this.view = view;
    }

    public String getOptions()
    {
        return options;
    }

    public void setOptions(String options)
    {
        this.options = options;
    }

    public String getType()
    {
        return PerforceClient.TYPE;
    }

    @Override
    public String getSummary()
    {
        String summary;

        if (useTemplateClient)
        {
            summary = spec;
        }
        else if (StringUtils.stringSet(stream))
        {
            summary = stream;
        }
        else
        {
            summary = view + "\n";
        }

        summary += "@";

        if (StringUtils.stringSet(port))
        {
            summary += port;
        }
        else
        {
            summary += "<default port>";
        }

        return summary;
    }

    public boolean getUseTicketAuth()
    {
        return useTicketAuth;
    }

    public void setUseTicketAuth(boolean useTicketAuth)
    {
        this.useTicketAuth = useTicketAuth;
    }

    public int getInactivityTimeout()
    {
        return inactivityTimeout;
    }

    public void setInactivityTimeout(int inactivityTimeout)
    {
        this.inactivityTimeout = inactivityTimeout;
    }

    public String getSyncWorkspacePattern()
    {
        return syncWorkspacePattern;
    }

    public void setSyncWorkspacePattern(String syncWorkspacePattern)
    {
        this.syncWorkspacePattern = syncWorkspacePattern;
    }

    public int getTimeOffset()
    {
        return timeOffset;
    }

    public void setTimeOffset(int timeOffset)
    {
        this.timeOffset = timeOffset;
    }

    public boolean isUnicodeServer()
    {
        return unicodeServer;
    }

    public void setUnicodeServer(boolean unicodeServer)
    {
        this.unicodeServer = unicodeServer;
    }

    public String getCharset()
    {
        return charset;
    }

    public void setCharset(String charset)
    {
        this.charset = charset;
    }
}
