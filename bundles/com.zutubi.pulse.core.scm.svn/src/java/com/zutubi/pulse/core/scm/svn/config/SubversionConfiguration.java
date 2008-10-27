package com.zutubi.pulse.core.scm.svn.config;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import com.zutubi.pulse.core.scm.svn.SubversionClient;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

/**
 * Subversion SCM configuration.
 */
@Form(fieldOrder = { "url", "username", "password", "keyfile", "keyfilePassphrase", "checkoutScheme", "filterPaths", "externalMonitorPaths", "verifyExternals", "monitor", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod" })
@ConfigurationCheck("SubversionConfigurationCheckHandler")
@SymbolicName("zutubi.subversionConfig")
public class SubversionConfiguration extends PollableScmConfiguration
{
    @Required
    @Constraint("SubversionUrlValidator")
    private String url;
    private String username;
    private String password;
    private String keyfile;
    @Password
    private String keyfilePassphrase;

    @Wizard.Ignore
    @StringList
    private List<String> externalMonitorPaths = new LinkedList<String>();
    @Wizard.Ignore
    private boolean verifyExternals;

    public SubversionConfiguration()
    {
    }

    public SubversionConfiguration(String url, String name, String password)
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
        return SubversionClient.TYPE;
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

    public List<String> getExternalMonitorPaths()
    {
        return externalMonitorPaths;
    }

    public void setExternalMonitorPaths(List<String> externalMonitorPaths)
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
