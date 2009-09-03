package com.zutubi.pulse.master.notifications.jabber.config;

import com.zutubi.pulse.master.notifications.jabber.JabberManager;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 *
 *
 */
@SymbolicName("zutubi.jabberConfig")
@Form(fieldOrder = {"enabled", "server", "port", "username", "password", "ssl"})
@ConfigurationCheck("JabberConfigurationCheckHandler")
@Classification(single = "jabber")
public class JabberConfiguration extends AbstractConfiguration
{
    @ControllingCheckbox
    private boolean enabled;
    
    @Required
    private String server;
    @Required
    private int port = JabberManager.DEFAULT_PORT;
    @Required
    private String username;
    private String password;
    private boolean ssl;

    public JabberConfiguration()
    {
        setPermanent(true);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

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
