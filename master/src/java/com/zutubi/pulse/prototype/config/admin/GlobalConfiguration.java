package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.config.Configuration;

/**
 * The global configuration scope, which holds server-wide configuration.
 */
public class GlobalConfiguration extends AbstractConfiguration
{
    public static final String SCOPE_NAME = "global";

    private GeneralAdminConfiguration generalConfig = new GeneralAdminConfiguration();
    private LoggingConfiguration loggingConfig = new LoggingConfiguration();
    private EmailConfiguration emailConfig = new EmailConfiguration();
    private LDAPConfiguration ldapConfig = new LDAPConfiguration();
    private JabberConfiguration jabberConfig = new JabberConfiguration();
    private LicenseConfiguration licenseConfig = new LicenseConfiguration();

    private ConfigurationPersistenceManager configurationPersistenceManager;

    public GeneralAdminConfiguration getGeneralConfig()
    {
        return generalConfig;
    }

    public void setGeneralConfig(GeneralAdminConfiguration generalConfig)
    {
        this.generalConfig = generalConfig;
    }

    public LoggingConfiguration getLoggingConfig()
    {
        return loggingConfig;
    }

    public void setLoggingConfig(LoggingConfiguration loggingConfig)
    {
        this.loggingConfig = loggingConfig;
    }

    public EmailConfiguration getEmailConfig()
    {
        return emailConfig;
    }

    public void setEmailConfig(EmailConfiguration emailConfig)
    {
        this.emailConfig = emailConfig;
    }

    public LDAPConfiguration getLdapConfig()
    {
        return ldapConfig;
    }

    public void setLdapConfig(LDAPConfiguration ldapConfig)
    {
        this.ldapConfig = ldapConfig;
    }

    public JabberConfiguration getJabberConfig()
    {
        return jabberConfig;
    }

    public void setJabberConfig(JabberConfiguration jabberConfig)
    {
        this.jabberConfig = jabberConfig;
    }

    public LicenseConfiguration getLicenseConfig()
    {
        return licenseConfig;
    }

    public void setLicenseConfig(LicenseConfiguration licenseConfig)
    {
        this.licenseConfig = licenseConfig;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public <T extends Configuration> T lookupExtendedConfig(String name, Class<T> clazz)
    {
        return configurationPersistenceManager.getInstance(PathUtils.getPath(SCOPE_NAME, name), clazz);
    }
}
