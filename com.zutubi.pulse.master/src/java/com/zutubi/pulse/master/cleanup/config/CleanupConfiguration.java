package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.dependency.DependencyManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.util.Constants;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.TextUtils;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

/**
 * The cleanup configuration defines the details for what and when a projects
 * build is cleaned up.
 */
@SymbolicName("zutubi.cleanupConfig")
@Form(fieldOrder = {"name", "what", "retain", "unit"})
@Table(columns = {"name", "what", "after", "states"})
public class CleanupConfiguration extends AbstractNamedConfiguration
{
    @Required
    @Format("CleanupWhatColumnFormatter")
    private CleanupWhat what;

    @Numeric(min = 1)
    private int retain = Integer.MIN_VALUE;

    @Required
    private CleanupUnit unit;

    @Select(optionProvider = "com.zutubi.pulse.master.tove.CompletedResultStateOptionProvider")
    @Format("CleanupStateColumnFormatter")
    private List<ResultState> states;

    @Select(optionProvider = "com.zutubi.pulse.master.tove.config.project.BuildStatusOptionProvider")
    private List<String> statuses = new LinkedList<String>();

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

    public List<BuildResult> getMatchingResults(Project project, BuildResultDao dao, DependencyManager dependencyManager)
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

        final List<String> allowedStatuses = new LinkedList<String>();
        if (statuses != null)
        {
            allowedStatuses.addAll(statuses);
        }
        if (allowedStatuses.size() == 0)
        {
            allowedStatuses.addAll(dependencyManager.getStatuses());
        }

        List<BuildResult> results = new LinkedList<BuildResult>();
        if(unit == CleanupUnit.BUILDS)
        {
            // See if there are too many builds of our states.  We assume here
            // we are called from within the build manager (so these two dao
            // calls are within the same transaction).
            int total = dao.getBuildCount(project, allowedStates, hasWorkDir);
            if(total > retain)
            {
                // Clean out the difference
                results.addAll(dao.queryBuilds(new Project[] { project }, allowedStates, 0, 0, hasWorkDir, 0, total - retain, false));
            }
        }
        else
        {
            long startTime = System.currentTimeMillis() - retain * Constants.DAY;
            results.addAll(dao.queryBuilds(new Project[] { project }, allowedStates, 0, startTime, hasWorkDir, -1, -1, false));
        }

        return CollectionUtils.filter(results, new Predicate<BuildResult>()
        {
            public boolean satisfied(BuildResult buildResult)
            {
                String status = buildResult.getStatus();
                if (!TextUtils.stringSet(status))
                {
                    status = "integration";
                }
                return allowedStatuses.contains(status);
            }
        });
    }
}
