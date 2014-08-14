package com.zutubi.pulse.master.license.config;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.license.License;
import com.zutubi.pulse.master.license.LicenseHolder;
import com.zutubi.pulse.master.license.LicenseType;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.util.EnumUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Shows the decoded license information to the user.
 */
public class LicenseConfigurationStateDisplay
{
    private ProjectManager projectManager;
    private AgentManager agentManager;
    private UserManager userManager;

    public List<String> getFields()
    {
        License license = LicenseHolder.getLicense();
        return Arrays.asList("type", "status", "name", license.isEvaluation() || license.getType() == LicenseType.SMALL_TEAM ? "expiry" : "supportExpiry", "supportedAgents", "supportedProjects", "supportedUsers");
    }

    public String formatName()
    {
        return LicenseHolder.getLicense().getHolder();
    }

    public String formatType()
    {
        return EnumUtils.toPrettyString(LicenseHolder.getLicense().getType());
    }

    public String formatSupportExpiry()
    {
        return formatExpiry();
    }

    public String formatExpiry()
    {
        License license = LicenseHolder.getLicense();
        Date expiryDate = license.getExpiryDate();

        if (expiryDate != null)
        {
            DateFormat dateFormatter = new SimpleDateFormat("EEEEE, dd MMM yyyy");

            Calendar cal = Calendar.getInstance();
            cal.setTime(expiryDate);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return dateFormatter.format(cal.getTime());
        }
        
        Messages messages = Messages.getInstance(LicenseConfiguration.class);
        return messages.format("no.expiry");
    }

    public String formatStatus()
    {
        License license = LicenseHolder.getLicense();
        if (license.isExpired())
        {
            Messages messages = Messages.getInstance(LicenseConfiguration.class);
            String key;
            if (license.isEvaluation())
            {
                key = "license.expired";
            }
            else if(license.canRunVersion(Version.getVersion()))
            {
                key = "license.support.expired";
            }
            else
            {
                key = "license.cannot.run.version";
            }
            return messages.format(key);
        }
        return "active";
    }

    public String formatSupportedUsers()
    {
        License license = LicenseHolder.getLicense();
        if (license.getSupportedUsers() == License.UNRESTRICTED)
        {
            return "unrestricted";
        }
        return String.format("%d of %d", userManager.getUserCount(), license.getSupportedUsers());
    }
    
    public String formatSupportedProjects()
    {
        License license = LicenseHolder.getLicense();
        if (license.getSupportedProjects() == License.UNRESTRICTED)
        {
            return "unrestricted";
        }
        return String.format("%d of %d", projectManager.getProjectCount(true), license.getSupportedProjects());
    }

    public String formatSupportedAgents()
    {
        License license = LicenseHolder.getLicense();
        if (license.getSupportedAgents() == License.UNRESTRICTED)
        {
            return "unrestricted";
        }
        return String.format("%d of %d", agentManager.getAgentCount(), license.getSupportedAgents());
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
