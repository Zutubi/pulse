package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.Constants;

import java.util.LinkedList;
import java.util.List;

/**
 * A retain rule is used to exclude matching builds from cleanup rules.
 */
@SymbolicName("zutubi.retainConfig")
@Form(fieldOrder = {"name", "retain", "unit", "states", "statuses"})
public class RetainConfiguration extends AbstractCleanupConfiguration
{
    @Override
    public List<BuildResult> getMatchingResults(Project project, BuildResultDao dao)
    {
        ResultState[] allowedStates = resolveAllowedStates();
        String[] allowedStatuses = resolveAllowedStatuses();

        List<BuildResult> results = new LinkedList<BuildResult>();
        if(unit == CleanupUnit.BUILDS)
        {
            results.addAll(dao.queryBuilds(new Project[]{project}, allowedStates, allowedStatuses, 0, 0, 0, retain, true, false));
        }
        else if (unit == CleanupUnit.DAYS)
        {
            long startTime = System.currentTimeMillis() - retain * Constants.DAY;
            results.addAll(dao.queryBuilds(new Project[]{project}, allowedStates, allowedStatuses, startTime, 0, -1, -1, true, false));
        }

        return results;
    }

    @Override
    public String summarise()
    {
        return "retain " + summariseFilter() + " for up to " + getRetain() + " " + getUnit().toString().toLowerCase();
    }
}
