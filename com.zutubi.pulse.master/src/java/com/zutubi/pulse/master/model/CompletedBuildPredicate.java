package com.zutubi.pulse.master.model;

import com.zutubi.util.Predicate;

/**
 * Tests builds and passes those that have completed.
 */
public class CompletedBuildPredicate implements Predicate<BuildResult>
{
    public boolean satisfied(BuildResult buildResult)
    {
        return buildResult.completed();
    }
}
