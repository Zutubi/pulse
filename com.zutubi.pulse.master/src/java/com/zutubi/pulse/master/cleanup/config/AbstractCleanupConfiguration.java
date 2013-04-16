package com.zutubi.pulse.master.cleanup.config;

import com.google.common.base.Function;
import com.zutubi.pulse.core.dependency.ivy.IvyStatus;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.tove.annotations.Format;
import com.zutubi.tove.annotations.Select;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.annotations.Numeric;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Lists.transform;

/**
 * Base for the common parts of cleanup and retain rules.
 */
@SymbolicName("zutubi.abstractCleanupConfig")
@Table(columns = {"name", "summary"})
public abstract class AbstractCleanupConfiguration extends AbstractNamedConfiguration
{
    @Numeric(min = 0)
    protected int retain;
    @Select(optionProvider = "com.zutubi.pulse.master.cleanup.config.CleanupUnitOptionProvider")
    protected CleanupUnit unit;
    @Select(optionProvider = "com.zutubi.pulse.master.tove.config.CompletedResultStateOptionProvider")
    @Format("CleanupStateColumnFormatter")
    protected List<ResultState> states;
    @Select(optionProvider = "com.zutubi.pulse.master.tove.config.project.BuildStatusOptionProvider")
    private List<String> statuses = new LinkedList<String>();

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

    protected String[] resolveAllowedStatuses()
    {
        String[] allowedStatuses = null;
        if (statuses != null)
        {
            allowedStatuses = statuses.toArray(new String[statuses.size()]);
        }
        if (allowedStatuses == null || allowedStatuses.length == 0)
        {
            allowedStatuses = IvyStatus.getStatuses().toArray(new String[IvyStatus.getStatuses().size()]);
        }
        return allowedStatuses;
    }

    protected ResultState[] resolveAllowedStates()
    {
        ResultState[] allowedStates = null;
        if (states != null)
        {
            allowedStates = states.toArray(new ResultState[states.size()]);
        }
        if(allowedStates == null || allowedStates.length == 0)
        {
            allowedStates = ResultState.getCompletedStates();
        }
        return allowedStates;
    }

    protected String summariseFilter()
    {
        String stateString;
        if (states == null || states.isEmpty())
        {
            stateString = "";
        }
        else
        {
            stateString = "states " + transform(states, new Function<ResultState, String>()
            {
                public String apply(ResultState input)
                {
                    return input.getPrettyString();
                }
            });
        }

        String statusString;
        if (statuses == null || statuses.isEmpty())
        {
            statusString = "";
        }
        else
        {
            statusString = "maturities " + statuses;
        }

        String filterString = StringUtils.join(" and ", false, true, stateString, statusString);
        if (filterString.length() > 0)
        {
            filterString = "builds in " + filterString;
        }
        else
        {
            filterString = "all builds";
        }

        return filterString;
    }

    public abstract List<BuildResult> getMatchingResults(Project project, BuildResultDao dao);
    public abstract String summarise();
}
