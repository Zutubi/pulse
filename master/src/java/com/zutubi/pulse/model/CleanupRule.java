/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.core.util.Constants;
import com.zutubi.pulse.model.persistence.BuildResultDao;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class CleanupRule extends Entity
{
    public enum CleanupUnit
    {
        BUILDS,
        DAYS
    }

    private boolean workDirOnly;
    private ResultState[] states;
    private int limit;
    private CleanupUnit unit;

    public CleanupRule()
    {

    }

    public CleanupRule(boolean workDirOnly, ResultState[] states, int count, CleanupUnit unit)
    {
        this.workDirOnly = workDirOnly;
        this.states = states;
        this.limit = count;
        this.unit = unit;
    }

    List<BuildResult> getMatchingResults(Project project, BuildResultDao dao)
    {
        Boolean hasWorkDir = null;
        if(workDirOnly)
        {
            hasWorkDir = true;
        }

        if(unit == CleanupUnit.BUILDS)
        {
            // See if there are too many builds of our states.  We assume here
            // we are called from within the build manager (so these two dao
            // calls are within the same transaction).
            int total = dao.getBuildCount(project, states, hasWorkDir);
            if(total > limit)
            {
                // Clean out the difference
                return dao.queryBuilds(new Project[] { project }, states, null, 0, 0, hasWorkDir, 0, total - limit, false);
            }
        }
        else
        {
            long startTime = System.currentTimeMillis() - limit * Constants.DAY;
            return dao.queryBuilds(new Project[] { project }, states, null, 0, startTime, hasWorkDir, -1, -1, false);
        }

        return new LinkedList<BuildResult>();
    }

    public boolean getWorkDirOnly()
    {
        return workDirOnly;
    }

    public void setWorkDirOnly(boolean workDirOnly)
    {
        this.workDirOnly = workDirOnly;
    }

    public ResultState[] getStates()
    {
        return states;
    }

    public void setStates(ResultState[] states)
    {
        this.states = states;
    }

    public String getStateNames()
    {
        String result = "";

        if(states != null)
        {
            for(ResultState state: states)
            {
                if(result.length() > 0)
                {
                    result += ",";
                }

                result += state.toString();
            }
        }

        return result;
    }

    public void setStateNames(String stateNames)
    {
        if(stateNames.length() > 0)
        {
            String [] names = stateNames.split(",");

            states = new ResultState[names.length];
            for(int i = 0; i < names.length; i++)
            {
                states[i] = ResultState.valueOf(names[i]);
            }
        }
        else
        {
            states = null;
        }
    }

    public String getPrettyStateNames()
    {
        if(states == null)
        {
            return "any";
        }
        else
        {
            String result = "";

            for(ResultState state: states)
            {
                if(result.length() > 0)
                {
                    result += ", ";
                }

                result += state.getPrettyString();
            }

            return result;
        }
    }

    public int getLimit()
    {
        return limit;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    public CleanupUnit getUnit()
    {
        return unit;
    }

    public void setUnit(CleanupUnit unit)
    {
        this.unit = unit;
    }

    public String getUnitName()
    {
        return unit.toString();
    }

    public void setUnitName(String name)
    {
        this.unit = CleanupUnit.valueOf(name);
    }

    public String getPrettyWhen()
    {
        if(unit == CleanupUnit.BUILDS)
        {
            return limit + " builds";
        }
        else
        {
            return  limit + " days";
        }
    }
}
