package com.zutubi.pulse.core.scm.svn.config;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import com.zutubi.pulse.core.scm.svn.SubversionClient;
import com.zutubi.tove.annotations.*;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

/**
 * Subversion SCM configuration.
 */
@Form(fieldOrder = { "url", "username", "password", "keyfile", "keyfilePassphrase", "cleanOnUpdateFailure", "useExport", "showChangedPaths", "externalsMonitoring", "externalMonitorPaths", "verifyExternals", "enableHttpSpooling", "monitor", "includedPaths", "excludedPaths", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod" })
@SymbolicName("zutubi.subversionConfig")
public class SubversionConfiguration extends PollableScmConfiguration implements Validateable
{
    private static final Messages I18N = Messages.getInstance(SubversionConfiguration.class);

    public enum ChangedPaths
    {
        NEVER,
        UPDATE_ONLY,
        ALWAYS,
    }

    /**
     * Describes if and how svn:externals will be monitored.
     */
    public enum ExternalsMonitoring
    {
        /**
         * Externals are not monitored for changes.
         */
        DO_NOT_MONITOR,
        /**
         * Externals will be scanned for in the whole directory tree, and any
         * externals found will have their trees scanned recursively.  All
         * externals found will be monitored for changes.
         */
        MONITOR_ALL,
        /**
         * Externals set on specific paths will be monitored for changes.  Only
         * first-level externals can be monitored in this way (i.e. not
         * externals referenced within externals).
         */
        MONITOR_SELECTED
    }

    @Required
    @Constraint("SubversionUrlValidator")
    private String url;
    private String username;
    @Password
    private String password;
    private String keyfile;
    @Password
    private String keyfilePassphrase;

    @Wizard.Ignore
    private boolean useExport;
    @Wizard.Ignore @Required
    private ChangedPaths showChangedPaths = ChangedPaths.ALWAYS;
    @Wizard.Ignore
    private boolean cleanOnUpdateFailure = true;
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

    public ChangedPaths getShowChangedPaths()
    {
        return showChangedPaths;
    }

    public void setShowChangedPaths(ChangedPaths showChangedPaths)
    {
        this.showChangedPaths = showChangedPaths;
    }

    public boolean isCleanOnUpdateFailure()
    {
        return cleanOnUpdateFailure;
    }

    public void setCleanOnUpdateFailure(boolean cleanOnUpdateFailure)
    {
        this.cleanOnUpdateFailure = cleanOnUpdateFailure;
    }

    public boolean isUseExport()
    {
        return useExport;
    }

    public void setUseExport(boolean useExport)
    {
        this.useExport = useExport;
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

    public void validate(ValidationContext context)
    {
        if (useExport)
        {
            if (externalsMonitoring != ExternalsMonitoring.DO_NOT_MONITOR)
            {
                context.addFieldError("externalsMonitoring", I18N.format("useExport.externals"));                
            }
        }
    }
}
