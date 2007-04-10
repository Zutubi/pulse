package com.zutubi.pulse.prototype.config;

/**
 *
 *
 */
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
}
