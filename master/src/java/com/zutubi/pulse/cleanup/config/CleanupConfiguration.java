package com.zutubi.pulse.cleanup.config;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.util.Constants;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
@SymbolicName("zutubi.cleanupConfig")
@Form(fieldOrder = {"name", "what", "retain", "unit"})
@Table(columns = {"name", "states", "after"})
public class CleanupConfiguration extends AbstractNamedConfiguration
{
    @Required
    private CleanupWhat what;

    @Numeric(min = 1)
    private int retain = Integer.MIN_VALUE;

    @Required
    private CleanupUnit unit;

    @Select(optionProvider = "com.zutubi.pulse.prototype.CompletedResultStateOptionProvider")
    @Format("CleanupStateColumnFormatter")
    private List<ResultState> states;


    public CleanupConfiguration(CleanupWhat what, List<ResultState> states, int count, CleanupUnit unit)
    {
        this.what = what;
        this.states = states;
        this.retain = count;
        this.unit = unit;
    }

    public CleanupConfiguration()
    {
    }

    public CleanupWhat getWhat()
    {
        return what;
    }

    public void setWhat(CleanupWhat what)
    {
        this.what = what;
    }

    public List<ResultState> getStates()
    {
        return states;
    }

    public void setStates(List<ResultState> states)
    {
        this.states = states;
    }

    public int getRetain()
    {
        return retain;
    }

    public void setRetain(int retain)
    {
        this.retain = retain;
    }

    public CleanupUnit getUnit()
    {
        return unit;
    }

    public void setUnit(CleanupUnit unit)
    {
        this.unit = unit;
    }

    public List<BuildResult> getMatchingResults(Project project, BuildResultDao dao)
    {
        Boolean hasWorkDir = null;
        if(what == CleanupWhat.WORKING_DIRECTORIES_ONLY)
        {
            hasWorkDir = true;
        }

        ResultState[] allowedStates = null;
        if (states != null)
        {
            allowedStates = states.toArray(new ResultState[states.size()]);
        }
        if(allowedStates == null || allowedStates.length == 0)
        {
            allowedStates = ResultState.getCompletedStates();
        }

        if(unit == CleanupUnit.BUILDS)
        {
            // See if there are too many builds of our states.  We assume here
            // we are called from within the build manager (so these two dao
            // calls are within the same transaction).
            int total = dao.getBuildCount(project, allowedStates, hasWorkDir);
            if(total > retain)
            {
                // Clean out the difference
                return dao.queryBuilds(new Project[] { project }, allowedStates, 0, 0, hasWorkDir, 0, total - retain, false);
            }
        }
        else
        {
            long startTime = System.currentTimeMillis() - retain * Constants.DAY;
            return dao.queryBuilds(new Project[] { project }, allowedStates, 0, startTime, hasWorkDir, -1, -1, false);
        }

        return new LinkedList<BuildResult>();
    }
}
