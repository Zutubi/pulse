package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.config.annotations.annotation.ConfigurationCheck;
import com.zutubi.config.annotations.annotation.Form;
import com.zutubi.config.annotations.annotation.Password;
import com.zutubi.pulse.prototype.record.SymbolicName;
import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

/**
 *
 *
 */
@SymbolicName("emailConfig")
@Form(fieldOrder = { "host", "ssl", "from", "username", "password", "subjectPrefix", "customPort", "port"})
@ConfigurationCheck("EmailConfigurationCheckHandler")
public class EmailConfiguration
{
    @Required
    private String host;
    private boolean ssl = false;
    @Required @Email
    private String from;
    private String username;
    @Password
    private String password;
    private String subjectPrefix;
    private boolean customPort;
    @Numeric(min = 1)
    private int port;

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public boolean getSsl()
    {
        return ssl;
    }

    public void setSsl(boolean ssl)
    {
        this.ssl = ssl;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
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

    public String getSubjectPrefix()
    {
        return subjectPrefix;
    }

    public void setSubjectPrefix(String subjectPrefix)
    {
        this.subjectPrefix = subjectPrefix;
    }

    public boolean isCustomPort()
    {
        return customPort;
    }

    public void setCustomPort(boolean customPort)
    {
        this.customPort = customPort;
    }

    public int getPort()
    {
        if(customPort)
        {
            return port;
        }
        else
        {
            return ssl ? 465 : 25;
        }
    }

    public void setPort(int port)
    {
        this.port = port;
    }
}
