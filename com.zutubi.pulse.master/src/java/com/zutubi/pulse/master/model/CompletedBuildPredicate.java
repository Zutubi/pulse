package com.zutubi.pulse.master.model;

import com.google.common.base.Predicate;

/**
 * Tests builds and passes those that have completed.
 */
public class CompletedBuildPredicate implements Predicate<BuildResult>
{
    public boolean apply(BuildResult buildResult)
    {
        return buildResult.completed();
    }
}
