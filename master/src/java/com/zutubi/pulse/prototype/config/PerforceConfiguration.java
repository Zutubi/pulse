package com.zutubi.pulse.prototype.config;

import com.zutubi.config.annotations.Form;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.ScmClient;
import com.zutubi.pulse.servercore.config.ScmConfiguration;

/**
 *
 *
 */
@Form(fieldOrder = { "port", "user", "password", "spec", "monitor", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod" })
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

    public ScmClient createClient() throws SCMException
    {
        // FIXME
        throw new RuntimeException("Method not yet implemented.");
    }
}
