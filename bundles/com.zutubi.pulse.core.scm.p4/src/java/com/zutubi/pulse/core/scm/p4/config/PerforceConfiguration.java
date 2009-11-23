package com.zutubi.pulse.core.scm.p4.config;

import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import com.zutubi.pulse.core.scm.p4.PerforceClient;
import com.zutubi.pulse.core.scm.p4.PerforceWorkspaceManager;
import com.zutubi.tove.annotations.*;
import com.zutubi.validation.annotations.Required;

/**
 * Configures details of a Perforce depot and client.
 */
@Form(fieldOrder = { "port", "user", "password", "useTemplateClient", "spec", "view", "useTicketAuth", "checkoutScheme", "monitor", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod", "filterPaths", "syncWorkspacePattern" })
@ConfigurationCheck("PerforceConfigurationCheckHandler")
@SymbolicName("zutubi.perforceConfig")
public class PerforceConfiguration extends PollableScmConfiguration
{
    @Required
    private String port = "perforce:1666";
    @Required
    private String user;
    private String password;
    @ControllingCheckbox(checkedFields = {"spec"}, uncheckedFields = {"view"})
    private boolean useTemplateClient = true;
    @Required
    private String spec;
    @TextArea(rows = 10, cols = 80) @Required
    private String view = "//depot/... //pulse/...";
    private boolean useTicketAuth = false;
    @Wizard.Ignore
    private String syncWorkspacePattern = PerforceWorkspaceManager.getWorkspacePrefix() + "$(project.handle)-$(agent.handle)";

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

    public String getView()
    {
        return view;
    }

    public void setView(String view)
    {
        this.view = view;
    }

    public String getType()
    {
        return PerforceClient.TYPE;
    }

    public boolean getUseTicketAuth()
    {
        return useTicketAuth;
    }

    public void setUseTicketAuth(boolean useTicketAuth)
    {
        this.useTicketAuth = useTicketAuth;
    }

    public String getSyncWorkspacePattern()
    {
        return syncWorkspacePattern;
    }

    public void setSyncWorkspacePattern(String syncWorkspacePattern)
    {
        this.syncWorkspacePattern = syncWorkspacePattern;
    }
}
