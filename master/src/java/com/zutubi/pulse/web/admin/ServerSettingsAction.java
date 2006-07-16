package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.bootstrap.MasterApplicationConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.jabber.JabberManager;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.security.ldap.LdapManager;
import com.zutubi.pulse.web.ActionSupport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 */
public class ServerSettingsAction extends ActionSupport
{
    private MasterConfigurationManager configurationManager;
    private JabberManager jabberManager;
    private MasterApplicationConfiguration config;
    private DateFormat dateFormatter = new SimpleDateFormat("EEEEE, dd MMM yyyy");
    private License license;
    private LdapManager ldapManager;
    private List<CommitMessageTransformer> commitMessageTransformers;
    private ProjectManager projectManager;

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

    public List<CommitMessageTransformer> getCommitMessageTransformers()
    {
        return commitMessageTransformers;
    }

    public String execute() throws Exception
    {
        config = configurationManager.getAppConfig();
        license = LicenseHolder.getLicense();
        commitMessageTransformers = projectManager.getCommitMessageTransformers();
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

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
