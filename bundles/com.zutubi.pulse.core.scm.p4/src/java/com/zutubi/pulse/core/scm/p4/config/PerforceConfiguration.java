package com.zutubi.pulse.core.scm.p4.config;

import com.zutubi.config.annotations.ConfigurationCheck;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.core.scm.p4.PerforceClient;
import com.zutubi.validation.annotations.Required;

/**
 * Configures details of a Perforce depot and client.
 */
@Form(fieldOrder = { "port", "user", "password", "spec", "monitor", "checkoutScheme", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod" })
@ConfigurationCheck("PerforceConfigurationCheckHandler")
@SymbolicName("zutubi.perforceConfig")
public class PerforceConfiguration extends ScmConfiguration
{
    @Required
    private String port;
    @Required
    private String user;
    private String password;
    @Required
    private String spec;

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

    public String getPreviousRevision(String revision)
    {
        long number = Long.valueOf(revision);
        if(number > 0)
        {
            return String.valueOf(number - 1);
        }
        return null;
    }
}
