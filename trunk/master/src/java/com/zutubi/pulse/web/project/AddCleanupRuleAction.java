package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.CleanupRule;

import java.util.LinkedList;

/**
 */
public class AddCleanupRuleAction extends CleanupRuleActionSupport
{
    public String doInput() throws Exception
    {
        project = getProjectManager().getProject(projectId);
        if(project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        stateNames = new LinkedList<String>();

        return INPUT;
    }

    public String execute() throws Exception
    {
        project = getProjectManager().getProject(projectId);
        if(project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        ResultState[] states = calculateResultStates();
        CleanupRule.CleanupUnit units = calculateCleanupUnits();

        CleanupRule rule = new CleanupRule(workDirOnly, states, limit, units);
        project.addCleanupRule(rule);
        getProjectManager().save(project);
        return SUCCESS;
    }
}
