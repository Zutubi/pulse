package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.bootstrap.MasterApplicationConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.jabber.JabberManager;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.security.ldap.LdapManager;
import com.zutubi.pulse.web.ActionSupport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 */
public class ServerSettingsAction extends ActionSupport
{
    private MasterConfigurationManager configurationManager;
    private JabberManager jabberManager;
    private LicenseManager licenseManager;
    private MasterApplicationConfiguration config;
    private DateFormat dateFormatter = new SimpleDateFormat("EEEEE, dd MMM yyyy");
    private License license;
    private LdapManager ldapManager;

    public MasterApplicationConfiguration getConfig()
    {
        return config;
    }

    public String getJabberStatus()
    {
        return jabberManager.getStatusMessage();
    }

    public String getLdapStatus()
    {
        return ldapManager.getStatusMessage();
    }

    public String execute() throws Exception
    {
        config = configurationManager.getAppConfig();
        license = licenseManager.getLicense();
        
        return SUCCESS;
    }

    public String getExpiryDate()
    {
        if (license.expires())
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(license.getExpiryDate());
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return dateFormatter.format(cal.getTime());
        }
        return "Never";
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }

    public void setLdapManager(LdapManager ldapManager)
    {
        this.ldapManager = ldapManager;
    }

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }
}
