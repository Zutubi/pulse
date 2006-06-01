package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.bootstrap.ApplicationConfiguration;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.jabber.JabberManager;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.license.License;

import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 */
public class ServerSettingsAction extends ActionSupport
{
    private ConfigurationManager configurationManager;
    private JabberManager jabberManager;
    private ApplicationConfiguration config;
    private DateFormat dateFormatter = new SimpleDateFormat("EEEEE, dd MMM yyyy");
    private License license;

    public ApplicationConfiguration getConfig()
    {
        return config;
    }

    public String getJabberStatus()
    {
        return jabberManager.getStatusMessage();
    }

    public String execute() throws Exception
    {
        config = configurationManager.getAppConfig();
        license = configurationManager.getData().getLicense();
        
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

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }
}
