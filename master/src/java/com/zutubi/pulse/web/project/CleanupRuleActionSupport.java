/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.CleanupRule;
import com.zutubi.pulse.model.Project;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class CleanupRuleActionSupport extends ProjectActionSupport
{
    public static final String UNITS_BUILDS = "builds";
    public static final String UNITS_DAYS = "days";

    protected long projectId;
    protected Project project;
    protected boolean workDirOnly = true;
    protected List<String> stateNames;
    protected String buildUnits = UNITS_BUILDS;
    protected int limit = 10;

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public Project getProject()
    {
        return project;
    }

    public Map<Boolean, String> getWhatMap()
    {
        Map<Boolean, String> result = new TreeMap<Boolean, String>();

        result.put(true, "working directories only");
        result.put(false, "whole build results");

        return result;
    }

    public boolean getWorkDirOnly()
    {
        return workDirOnly;
    }

    public void setWorkDirOnly(boolean workDirOnly)
    {
        this.workDirOnly = workDirOnly;
    }

    public Map<String, String> getStateMap()
    {
        Map<String, String> result = new TreeMap<String, String>();

        result.put(ResultState.ERROR.toString(), "error");
        result.put(ResultState.FAILURE.toString(), "failure");
        result.put(ResultState.SUCCESS.toString(), "success");

        return result;
    }

    public List<String> getStateNames()
    {
        return stateNames;
    }

    public void setStateNames(List<String> stateNames)
    {
        this.stateNames = stateNames;
    }

    public int getLimit()
    {
        return limit;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    public String getBuildUnits()
    {
        return buildUnits;
    }

    public List<String> getUnitsList()
    {
        List<String> units = new LinkedList<String>();

        units.add(UNITS_BUILDS);
        units.add(UNITS_DAYS);

        return units;
    }

    public void setBuildUnits(String buildUnits)
    {
        this.buildUnits = buildUnits;
    }

    protected ResultState[] calculateResultStates()
    {
        ResultState[] states = null;

        if(stateNames != null && stateNames.size() > 0)
        {
            states = new ResultState[stateNames.size()];
            for(int i = 0; i < states.length; i++)
            {
                states[i] = ResultState.valueOf(stateNames.get(i));
            }
        }
        return states;
    }

    protected CleanupRule.CleanupUnit calculateCleanupUnits()
    {
        CleanupRule.CleanupUnit units;
        if(buildUnits.equals(EditCleanupRuleAction.UNITS_BUILDS))
        {
            units = CleanupRule.CleanupUnit.BUILDS;
        }
        else
        {
            units = CleanupRule.CleanupUnit.DAYS;
        }
        return units;
    }
}
