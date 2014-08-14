package com.zutubi.pulse.master.xwork.actions;

import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.license.License;
import com.zutubi.pulse.master.license.LicenseHolder;

import java.util.LinkedList;

/**
 */
public class NotLicensedErrorAction extends ActionSupport
{
    private LinkedList<LicenseRestriction> restrictions;

    private AgentManager agentManager;

    public String doInput() throws Exception
    {
        return execute();
    }

    public String execute() throws Exception
    {
        License license = LicenseHolder.getLicense();
        restrictions = new LinkedList<LicenseRestriction>();
        restrictions.add(new LicenseRestriction("agents", license.getSupportedAgents(), agentManager.getAgentCount()));
        restrictions.add(new LicenseRestriction("projects", license.getSupportedProjects(), projectManager.getProjectCount(true)));
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

    public static class LicenseRestriction
    {
        private String entity;
        private int supported;
        private int inUse;

        public LicenseRestriction(String entity, int supported, int inUse)
        {
            this.entity = entity;
            this.supported = supported;
            this.inUse = inUse;
        }

        public String getEntity()
        {
            return entity;
        }

        public int getSupported()
        {
            return supported;
        }

        public int getInUse()
        {
            return inUse;
        }
    }
}
