package com.zutubi.pulse.scm.svn.config;

import com.opensymphony.util.TextUtils;
import com.zutubi.config.annotations.ConfigurationCheck;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Wizard;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.scm.svn.SvnClient;
import com.zutubi.pulse.scm.config.ScmConfiguration;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.annotations.Required;

/**
 */
@Form(fieldOrder = { "url", "username", "password", "keyfile", "keyfilePassphrase", "externalMonitorPaths", "verifyExternals", "monitor", "checkoutScheme", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod" })
@ConfigurationCheck("SvnConfigurationCheckHandler")
@SymbolicName("zutubi.svnConfig")
public class SvnConfiguration extends ScmConfiguration
{
    @Required
    private String url;
    private String username;
    private String password;
    private String keyfile;
    private String keyfilePassphrase;

    // FIXME: add a validator that splits this field
    @Wizard.Ignore
    private String externalMonitorPaths;
    @Wizard.Ignore
    private boolean verifyExternals;

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

    public SvnClient createClient() throws ScmException
    {
        SvnClient client;
        if (!TextUtils.stringSet(keyfile))
        {
            if (TextUtils.stringSet(username))
            {
                client = new SvnClient(url, username, password);
            }
            else
            {
                client = new SvnClient(url);
            }
        }
        else
        {
            if (TextUtils.stringSet(keyfilePassphrase))
            {
                client = new SvnClient(url, username, password, keyfile, keyfilePassphrase);
            }
            else
            {
                client = new SvnClient(url, username, password, keyfile);
            }
        }

        client.setExcludedPaths(getFilterPaths());

        if(TextUtils.stringSet(externalMonitorPaths))
        {
            for(String path: StringUtils.split(externalMonitorPaths))
            {
                client.addExternalPath(path);
            }
        }

        client.setVerifyExternals(getVerifyExternals());
        return client;
    }

    public String getExternalMonitorPaths()
    {
        return externalMonitorPaths;
    }

    public void setExternalMonitorPaths(String externalMonitorPaths)
    {
        this.externalMonitorPaths = externalMonitorPaths;
    }

    public boolean getVerifyExternals()
    {
        return verifyExternals;
    }

    public void setVerifyExternals(boolean verifyExternals)
    {
        this.verifyExternals = verifyExternals;
    }
}
