package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.license.License;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.UserManager;

import java.util.Date;

/**
 * <class-comment/>
 */
public class CommercialLicenseAuthorisation extends AbstractLicenseAuthorisation
{
    private ProjectManager projectManager;
    private UserManager userManager;
    private AgentManager agentManager;

    public boolean canRunPulse()
    {
        License l = provider.getLicense();
        if (!l.isExpired())
        {
            return true;
        }

        // can continue to run pulse on patch releases.
        Version currentVersion = Version.getVersion();
        Date vrd = currentVersion.getReleaseDateAsDate();
        Date expiry = l.getExpiryDate();

        return (vrd.getTime() < expiry.getTime());
    }

    public boolean canAddProject()
    {
        License l = provider.getLicense();
        if (!canRunPulse())
        {
            return false;
        }
        int supportedProjects = l.getSupportedProjects();
        if (supportedProjects == License.UNRESTRICTED)
        {
            return true;
        }
        return supportedProjects > projectManager.getProjectCount();
    }

    public boolean canAddUser()
    {
        License l = provider.getLicense();
        if (!canRunPulse())
        {
            return false;
        }
        int supportedUsers = l.getSupportedUsers();
        if (supportedUsers == License.UNRESTRICTED)
        {
            return true;
        }
        return supportedUsers > userManager.getUserCount();
    }

    public boolean canAddAgent()
    {
        License l = provider.getLicense();
        if (!canRunPulse())
        {
            return false;
        }
        int supportedAgents = l.getSupportedAgents();
        if (supportedAgents == License.UNRESTRICTED)
        {
            return true;
        }
        return supportedAgents > agentManager.getAgentCount();
    }

    /**
     * Required resource.
     *
     * @param projectManager
     */
    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    /**
     * Required resource.
     *
     * @param userManager
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    /**
     * Required resource.
     *
     * @param agentManager
     */
    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
