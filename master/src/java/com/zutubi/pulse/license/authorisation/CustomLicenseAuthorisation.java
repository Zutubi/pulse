package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.UserManager;

/**
 * <class-comment/>
 */
public class CustomLicenseAuthorisation extends AbstractLicenseAuthorisation
{
    private ProjectManager projectManager;
    private UserManager userManager;
    private AgentManager agentManager;

    public boolean canRunPulse()
    {
        License l = getProvider().getLicense();
        return !l.isExpired();
    }

    public boolean canAddProject()
    {
        License l = getProvider().getLicense();
        if (!canRunPulse())
        {
            return false;
        }
        int supportedProjects = l.getSupportedProjects();
        if (isUnrestricted(supportedProjects))
        {
            return true;
        }
        return supportedProjects > projectManager.getProjectCount();
    }

    public boolean canAddUser()
    {
        License l = getProvider().getLicense();
        if (!canRunPulse())
        {
            return false;
        }
        int supportedUsers = l.getSupportedUsers();
        if (isUnrestricted(supportedUsers))
        {
            return true;
        }
        return supportedUsers > userManager.getUserCount();
    }

    public boolean canAddAgent()
    {
        License l = getProvider().getLicense();
        if (!canRunPulse())
        {
            return false;
        }
        int supportedAgents = l.getSupportedAgents();
        if (isUnrestricted(supportedAgents))
        {
            return true;
        }
        return supportedAgents > agentManager.getAgentCount();
    }

    private boolean isUnrestricted(int i)
    {
        return i == License.UNRESTRICTED;
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
