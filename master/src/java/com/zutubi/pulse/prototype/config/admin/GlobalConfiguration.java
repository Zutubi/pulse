package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.config.annotations.Classification;
import com.zutubi.config.annotations.ControllingCheckbox;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.jabber.config.JabberConfiguration;
import com.zutubi.pulse.license.config.LicenseConfiguration;
import com.zutubi.util.TextUtils;

/**
 * The global configuration scope, which holds server-wide configuration.
 */
@SymbolicName("zutubi.globalConfig")
@Classification(single = "settings")
@Form(fieldOrder={"baseUrl", "masterHost", "baseHelpUrl", "rssEnabled", "anonymousAccessEnabled", "anonymousSignupEnabled", "scmPollingInterval", "recipeTimeoutEnabled", "recipeTimeout" })
public class GlobalConfiguration extends AbstractConfiguration
{
    public static final String SCOPE_NAME = "settings";

    private String baseUrl;
    private String masterHost;
    private String baseHelpUrl = "http://confluence.zutubi.com/display/pulse0200";
    private boolean rssEnabled = true;
    private boolean anonymousAccessEnabled = false;
    private boolean anonymousSignupEnabled = false;
    private int scmPollingInterval = 5;

    @ControllingCheckbox(dependentFields = {"recipeTimeout"})
    private boolean recipeTimeoutEnabled = true;
    private int recipeTimeout = 15;

    private LoggingConfiguration logging = new LoggingConfiguration();
    private EmailConfiguration email = new EmailConfiguration();
    private LDAPConfiguration ldap = new LDAPConfiguration();
    private JabberConfiguration jabber = new JabberConfiguration();
    private LicenseConfiguration license = new LicenseConfiguration();

    private ConfigurationTemplateManager configurationTemplateManager;

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        // munge the url a little. We assume that there is no trailing '/' when using this property.
        if (TextUtils.stringSet(baseUrl) && baseUrl.endsWith("/"))
        {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.baseUrl = baseUrl;
    }

    public String getMasterHost()
    {
        return masterHost;
    }

    public void setMasterHost(String masterHost)
    {
        this.masterHost = masterHost;
    }

    public String getBaseHelpUrl()
    {
        return baseHelpUrl;
    }

    public void setBaseHelpUrl(String baseHelpUrl)
    {
        this.baseHelpUrl = baseHelpUrl;
    }

    public boolean isRssEnabled()
    {
        return rssEnabled;
    }

    public void setRssEnabled(boolean rssEnabled)
    {
        this.rssEnabled = rssEnabled;
    }

    public boolean isAnonymousAccessEnabled()
    {
        return anonymousAccessEnabled;
    }

    public void setAnonymousAccessEnabled(boolean anonymousAccessEnabled)
    {
        this.anonymousAccessEnabled = anonymousAccessEnabled;
    }

    public boolean isAnonymousSignupEnabled()
    {
        return anonymousSignupEnabled;
    }

    public void setAnonymousSignupEnabled(boolean anonymousSignupEnabled)
    {
        this.anonymousSignupEnabled = anonymousSignupEnabled;
    }

    public int getScmPollingInterval()
    {
        return scmPollingInterval;
    }

    public void setScmPollingInterval(int scmPollingInterval)
    {
        this.scmPollingInterval = scmPollingInterval;
    }

    public boolean isRecipeTimeoutEnabled()
    {
        return recipeTimeoutEnabled;
    }

    public void setRecipeTimeoutEnabled(boolean recipeTimeoutEnabled)
    {
        this.recipeTimeoutEnabled = recipeTimeoutEnabled;
    }

    public int getRecipeTimeout()
    {
        return recipeTimeout;
    }

    public void setRecipeTimeout(int recipeTimeout)
    {
        this.recipeTimeout = recipeTimeout;
    }

    public LoggingConfiguration getLogging()
    {
        return logging;
    }

    public void setLogging(LoggingConfiguration logging)
    {
        this.logging = logging;
    }

    public EmailConfiguration getEmail()
    {
        return email;
    }

    public void setEmail(EmailConfiguration email)
    {
        this.email = email;
    }

    public LDAPConfiguration getLdap()
    {
        return ldap;
    }

    public void setLdap(LDAPConfiguration ldap)
    {
        this.ldap = ldap;
    }

    public JabberConfiguration getJabber()
    {
        return jabber;
    }

    public void setJabber(JabberConfiguration jabber)
    {
        this.jabber = jabber;
    }

    public LicenseConfiguration getLicense()
    {
        return license;
    }

    public void setLicense(LicenseConfiguration license)
    {
        this.license = license;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public <T extends Configuration> T lookupExtendedConfig(String name, Class<T> clazz)
    {
        return configurationTemplateManager.getInstance(PathUtils.getPath(SCOPE_NAME, name), clazz);
    }
}
