package com.zutubi.pulse.scm.p4.config;

import com.zutubi.config.annotations.ConfigurationCheck;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.scm.config.ScmConfiguration;
import com.zutubi.pulse.scm.p4.PerforceClient;

/**
 *
 *
 */
@Form(fieldOrder = { "port", "user", "password", "spec", "monitor", "checkoutScheme", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod" })
@ConfigurationCheck("com.zutubi.pulse.prototype.config.PerforceConfigurationCheckHandler")
@SymbolicName("zutubi.perforceConfig")
public class PerforceConfiguration extends ScmConfiguration
{
    private String port;
    private String user;
    private String password;
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
        return "p4";
    }

    public PerforceClient createClient() throws ScmException
    {
        PerforceClient client = new PerforceClient(port, user, password, spec);
        client.setExcludedPaths(getFilterPaths());
        return client;
    }
}
