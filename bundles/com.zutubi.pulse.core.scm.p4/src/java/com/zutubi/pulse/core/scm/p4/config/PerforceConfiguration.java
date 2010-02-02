package com.zutubi.pulse.core.scm.p4.config;

import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import com.zutubi.pulse.core.scm.p4.PerforceClient;
import com.zutubi.tove.annotations.*;
import com.zutubi.validation.annotations.Required;

/**
 * Configures details of a Perforce depot and client.
 */
@Form(fieldOrder = { "port", "user", "password", "spec", "useTicketAuth", "unicodeServer", "charset", "checkoutScheme", "monitor", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod" , "timeOffset"})
@ConfigurationCheck("PerforceConfigurationCheckHandler")
@SymbolicName("zutubi.perforceConfig")
public class PerforceConfiguration extends PollableScmConfiguration
{
    @Required
    private String port = "perforce:1666";
    @Required
    private String user;
    private String password;
    @Required
    private String spec;
    private boolean useTicketAuth = false;
    @Wizard.Ignore
    private int timeOffset= 0;

    @ControllingCheckbox(dependentFields = {"charset"})
    private boolean unicodeServer = false;
    @Required
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

    public String getSpec()
    {
        return spec;
    }

    public void setSpec(String spec)
    {
        this.spec = spec;
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
