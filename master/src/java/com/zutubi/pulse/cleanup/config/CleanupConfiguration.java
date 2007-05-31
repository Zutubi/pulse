package com.zutubi.pulse.cleanup.config;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Select;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Format;
import com.zutubi.config.annotations.Table;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.util.Constants;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
@SymbolicName("cleanupRuleConfig")
@Form(fieldOrder = {"name", "what", "retain", "unit"})
@Table(columns = {"name", "states", "when"})
public class CleanupConfiguration extends AbstractNamedConfiguration
{
    private CleanupWhat what;

    @Select(optionProvider = "com.zutubi.pulse.prototype.CompletedResultStateOptionProvider")
    @Format("CleanupStateColumnFormatter")
    private List<ResultState> states;

    private int retain;

    private CleanupUnit unit;

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
