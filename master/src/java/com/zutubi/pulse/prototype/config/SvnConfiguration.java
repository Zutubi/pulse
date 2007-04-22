package com.zutubi.pulse.prototype.config;

import com.zutubi.config.annotations.ConfigurationCheck;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.scm.ScmClient;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.servercore.config.ScmConfiguration;

/**
 */
@Form(fieldOrder = { "url", "username", "password", "keyfile", "keyfilePassphrase"})
@ConfigurationCheck("SvnConfigurationCheckHandler")
public class SvnConfiguration extends ScmConfiguration
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

    public String getType()
    {
        return "svn";
    }

    public ScmClient createClient() throws SCMException
    {
        // FIXME
        throw new RuntimeException("Method not yet implemented.");
    }
}
