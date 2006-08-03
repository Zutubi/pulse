package com.zutubi.pulse.web;

import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.web.admin.ServerSettingsAction.LicenseRestriction;

import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class NotLicensedErrorAction extends ActionSupport
{
    private LinkedList<LicenseRestriction> restrictions;

    private AgentManager agentManager;
    private ProjectManager projectManager;
    private UserManager userManager;

    public String doInput() throws Exception
    {
        return execute();
    }

    public String execute() throws Exception
    {
        License license = LicenseHolder.getLicense();
        restrictions = new LinkedList<LicenseRestriction>();
        restrictions.add(new LicenseRestriction("agents", license.getSupportedAgents(), agentManager.getAgentCount()));
        restrictions.add(new LicenseRestriction("projects", license.getSupportedProjects(), projectManager.getProjectCount()));
        restrictions.add(new LicenseRestriction("users", license.getSupportedUsers(), userManager.getUserCount()));

        return SUCCESS;
    }

    public LinkedList<LicenseRestriction> getRestrictions()
    {
        return restrictions;
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
}
