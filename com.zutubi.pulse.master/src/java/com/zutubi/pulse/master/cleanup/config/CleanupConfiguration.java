package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.pulse.core.dependency.DependencyManager;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.util.Constants;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

/**
 * The cleanup configuration defines how builds are cleaned up for a project.  This includes
 * both when cleanup occurs, and what parts of the builds are cleaned up.
 */
@SymbolicName("zutubi.cleanupConfig")
@Form(fieldOrder = {"name", "cleanupAll", "what", "retain", "unit", "states", "statuses"})
@Table(columns = {"name", "what", "after", "states"})
public class CleanupConfiguration extends AbstractNamedConfiguration
{
    @ControllingCheckbox(dependentFields = {"what"}, invert = true)
    private boolean cleanupAll = true;

    @Required
    @Format("CleanupWhatColumnFormatter")
    private List<CleanupWhat> what;

    @Numeric(min = 0)
    private int retain;

    @Select(optionProvider = "com.zutubi.pulse.master.cleanup.config.CleanupUnitOptionProvider")
    private CleanupUnit unit;

    @Select(optionProvider = "com.zutubi.pulse.master.tove.CompletedResultStateOptionProvider")
    @Format("CleanupStateColumnFormatter")
    private List<ResultState> states;

    @Select(optionProvider = "com.zutubi.pulse.master.tove.config.project.BuildStatusOptionProvider")
    private List<String> statuses = new LinkedList<String>();

    public CleanupConfiguration(CleanupWhat what, List<ResultState> states, int count, CleanupUnit unit)
    {
        this.what = new LinkedList<CleanupWhat>();
        if (what != null)
        {
            this.what.add(what);
            this.cleanupAll = false;
        }
        else
        {
            this.cleanupAll = true;
        }
        this.states = states;
        this.retain = count;
        this.unit = unit;
    }

    public CleanupConfiguration()
    {
    }

    public List<CleanupWhat> getWhat()
    {
        return what;
    }

    public void setWhat(List<CleanupWhat> what)
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

    public List<String> getStatuses()
    {
        return statuses;
    }

    public void setStatuses(List<String> statuses)
    {
        this.statuses = statuses;
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

    public boolean isCleanupAll()
    {
        return cleanupAll;
    }

    public void setCleanupAll(boolean cleanupAll)
    {
        this.cleanupAll = cleanupAll;
    }

    public List<BuildResult> getMatchingResults(Project project, BuildResultDao dao, DependencyManager dependencyManager)
    {
        // The build query allows us to filter on builds that have working directories or not.  If
        // the cleanup rule is only for builds with working directories, we can make use of this filter
        // to optimise the query.  If things other than the working directories are being cleaned, then
        // we can not make use of the filter.
        Boolean filterHasWorkDir = null;
        if(what != null && what.size() == 1 && what.contains(CleanupWhat.WORKING_COPY_SNAPSHOT))
        {
            filterHasWorkDir = true;
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

        String[] allowedStatuses = null;
        if (statuses != null)
        {
            allowedStatuses = statuses.toArray(new String[statuses.size()]);
        }
        if (allowedStatuses == null || allowedStatuses.length == 0)
        {
            allowedStatuses = dependencyManager.getStatuses().toArray(new String[dependencyManager.getStatuses().size()]);
        }

        List<BuildResult> results = new LinkedList<BuildResult>();
        if(unit == CleanupUnit.BUILDS)
        {
            // See if there are too many builds of our states.  We assume here
            // we are called from within the build manager (so these two dao
            // calls are within the same transaction).
            int total = dao.getBuildCount(project, allowedStates, filterHasWorkDir);
            if(total > retain)
            {
                // Clean out the difference
                results.addAll(dao.queryBuilds(new Project[] { project }, allowedStates, allowedStatuses, 0, 0, filterHasWorkDir, 0, total - retain, false));
            }
        }
        else
        {
            long startTime = System.currentTimeMillis() - retain * Constants.DAY;
            results.addAll(dao.queryBuilds(new Project[] { project }, allowedStates, allowedStatuses, 0, startTime, filterHasWorkDir, -1, -1, false));
        }
        return results;
    }
}
