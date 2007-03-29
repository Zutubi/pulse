package com.zutubi.pulse.prototype.config;

import com.zutubi.pulse.form.descriptor.annotation.Summary;
import com.zutubi.prototype.annotation.Form;
import com.zutubi.prototype.annotation.ConfigurationCheck;

/**
 */
@Summary(fields = {"name", "url"})
@Form(fieldOrder = { "url", "username", "password", "keyfile", "keyfilePassphrase"})
@ConfigurationCheck(SvnConfigurationCheckHandler.class)
public class SvnConfiguration extends BaseScmConfiguration
{
    private String url;

    private String username;

    private String password;

    private String keyfile;
    
    private String keyfilePassphrase;

    public SvnConfiguration()
    {
    }

    public SvnConfiguration(String url, String name, String password)
    {
        this.url = url;
        this.username = name;
        this.password = password;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
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

    public String getKeyfile()
    {
        return keyfile;
    }

    public void setKeyfile(String keyfile)
    {
        this.keyfile = keyfile;
    }

    public String getKeyfilePassphrase()
    {
        return keyfilePassphrase;
    }

    public void setKeyfilePassphrase(String keyfilePassphrase)
    {
        this.keyfilePassphrase = keyfilePassphrase;
    }
}
