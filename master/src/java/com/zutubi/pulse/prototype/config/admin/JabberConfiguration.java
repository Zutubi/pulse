package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.prototype.annotation.ConfigurationCheck;
import com.zutubi.prototype.annotation.Form;
import com.zutubi.pulse.jabber.JabberManager;
import com.zutubi.pulse.prototype.record.SymbolicName;
import com.zutubi.validation.annotations.Required;

/**
 *
 *
 */
@SymbolicName("jabberConfig")
@Form(fieldOrder = {"server", "port", "username", "password", "ssl"})
@ConfigurationCheck(JabberConfigurationCheckHandler.class)
public class JabberConfiguration
{
    @Required
    private String server;
    @Required
    private int port = JabberManager.DEFAULT_PORT;
    @Required
    private String username;
    private String password;
    private boolean ssl;

    public String getServer()
    {
        return server;
    }

    public void setServer(String server)
    {
        this.server = server;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean isSsl()
    {
        return ssl;
    }

    public void setSsl(boolean ssl)
    {
        this.ssl = ssl;
    }
}
