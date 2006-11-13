package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.CleanupRule;

import java.util.LinkedList;

/**
 */
public class EditCleanupRuleAction extends CleanupRuleActionSupport
{
    private long id;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String doInput() throws Exception
    {
        project = getProjectManager().getProject(projectId);
        if(project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        CleanupRule rule = project.getCleanupRule(id);
        if(rule == null)
        {
            addActionError("Unknown cleanup rule [" + id + "]");
            return ERROR;
        }

        workDirOnly = rule.getWorkDirOnly();

        ResultState[] states = rule.getStates();
        if(states != null)
        {
            stateNames = new LinkedList<String>();
            for(ResultState state: states)
            {
                stateNames.add(state.toString());
            }
        }

        this.limit = rule.getLimit();

        CleanupRule.CleanupUnit unit = rule.getUnit();
        if(unit == CleanupRule.CleanupUnit.BUILDS)
        {
            this.buildUnits = UNITS_BUILDS;
        }
        else
        {
            this.buildUnits = UNITS_DAYS;
        }

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

        CleanupRule rule = project.getCleanupRule(id);
        if(rule == null)
        {
            addActionError("Unknown cleanup rule [" + id + "]");
            return ERROR;
        }

        ResultState[] states = calculateResultStates();
        CleanupRule.CleanupUnit units = calculateCleanupUnits();

        rule.setWorkDirOnly(workDirOnly);
        rule.setStates(states);
        rule.setLimit(limit);
        rule.setUnit(units);

        getProjectManager().save(project);
        return SUCCESS;
    }

}
