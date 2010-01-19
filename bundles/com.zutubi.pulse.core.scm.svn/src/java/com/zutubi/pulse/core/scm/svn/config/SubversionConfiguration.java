package com.zutubi.pulse.core.scm.svn.config;

import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import com.zutubi.pulse.core.scm.svn.SubversionClient;
import com.zutubi.tove.annotations.*;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

/**
 * Subversion SCM configuration.
 */
@Form(fieldOrder = { "url", "username", "password", "keyfile", "keyfilePassphrase", "checkoutScheme", "filterPaths", "externalsMonitoring", "externalMonitorPaths", "verifyExternals", "enableHttpSpooling", "monitor", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod" })
@ConfigurationCheck("SubversionConfigurationCheckHandler")
@SymbolicName("zutubi.subversionConfig")
public class SubversionConfiguration extends PollableScmConfiguration
{
    public enum ExternalsMonitoring
    {
        DO_NOT_MONITOR,
        MONITOR_ALL,
        MONITOR_SELECTED
    }

    @Required
    @Constraint("SubversionUrlValidator")
    private String url;
    private String username;
    private String password;
    private String keyfile;
    @Password
    private String keyfilePassphrase;

    @Wizard.Ignore
    @ControllingSelect(dependentFields = {"externalMonitorPaths"}, enableSet = {"MONITOR_SELECTED"})
    private ExternalsMonitoring externalsMonitoring = ExternalsMonitoring.DO_NOT_MONITOR;
    @Wizard.Ignore
    @StringList
    private List<String> externalMonitorPaths = new LinkedList<String>();
    @Wizard.Ignore
    private boolean verifyExternals = true;
    @Wizard.Ignore
    private boolean enableHttpSpooling;

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

    public ExternalsMonitoring getExternalsMonitoring()
    {
        return externalsMonitoring;
    }

    public void setExternalsMonitoring(ExternalsMonitoring externalsMonitoring)
    {
        this.externalsMonitoring = externalsMonitoring;
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

    public boolean isEnableHttpSpooling()
    {
        return enableHttpSpooling;
    }

    public void setEnableHttpSpooling(boolean enableHttpSpooling)
    {
        this.enableHttpSpooling = enableHttpSpooling;
    }
}
