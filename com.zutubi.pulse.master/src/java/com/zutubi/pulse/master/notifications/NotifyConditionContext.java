package com.zutubi.pulse.master.notifications;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.ChangelistManager;
import com.zutubi.pulse.master.model.UpstreamChangelist;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Context in which notification conditions are evaluated.  Sharing this context among conditions eliminates the
 * likelihood of repeated queries for the same information.  Fields are lazily initialised as required to evaluate
 * conditions.
 */
public class NotifyConditionContext
{
    private BuildResult buildResult;
    private BuildResult lastSuccess;
    private boolean lastSuccessInitialised;
    private BuildResult lastHealthy;
    private boolean lastHealthyInitialised;
    private List<PersistentChangelist> directChanges;
    private List<PersistentChangelist> upstreamChanges;
    private List<PersistentChangelist> changesSinceLastSuccess;
    private List<PersistentChangelist> upstreamChangesSinceLastSuccess;
    private List<PersistentChangelist> changesSinceLastHealthy;
    private List<PersistentChangelist> upstreamChangesSinceLastHealthy;

    private NotifyConditionContext previous;

    private BuildManager buildManager;
    private ChangelistManager changelistManager;

    /**
     * Creates a context around a given build result.
     *
     * @param buildResult the build result conditions are applied to, may be null (with suitable null/empty results from
     *                    all methods)
     */
    public NotifyConditionContext(BuildResult buildResult)
    {
        this.buildResult = buildResult;
    }

    /**
     * @return the build to apply conditions to, may be null
     */
    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    /**
     * @return the most recent successful build before the build of this context, may be null
     */
    public BuildResult getLastSuccess()
    {
        if (!lastSuccessInitialised)
        {
            if (buildResult == null)
            {
                lastSuccess = null;
            }
            else if (lastSuccess.getState() == ResultState.SUCCESS)
            {
                lastSuccess = buildResult;
            }
            else
            {
                lastSuccess = buildManager.getPreviousBuildResultWithRevision(buildResult, new ResultState[] { ResultState.SUCCESS });
            }

            lastSuccessInitialised = true;
        }

        return lastSuccess;
    }

    /**
     * @return the most recent healthy build before the build of this context, may be null
     */
    public BuildResult getLastHealthy()
    {
        if (!lastHealthyInitialised)
        {
            if (buildResult == null)
            {
                lastHealthy = null;
            }
            else if (lastHealthy.getState().isHealthy())
            {
                lastHealthy = buildResult;
            }
            else
            {
                lastHealthy = buildManager.getPreviousBuildResultWithRevision(buildResult, ResultState.getHealthyStates());
            }

            lastHealthyInitialised = true;
        }

        return lastHealthy;
    }

    /**
     * @return changelists directly attributed to the build for this context
     */
    public List<PersistentChangelist> getChanges()
    {
        if (directChanges == null)
        {
            directChanges = buildResult == null ? Collections.<PersistentChangelist>emptyList() : changelistManager.getChangesForBuild(buildResult, 0, false);
        }

        return directChanges;
    }

    /**
     * @return upstream changelists that indirectly affected the build for this context
     */
    public List<PersistentChangelist> getUpstreamChanges()
    {
        if (upstreamChanges == null)
        {
            upstreamChanges = buildResult == null ? Collections.<PersistentChangelist>emptyList() :
                    newArrayList(transform(changelistManager.getUpstreamChangelists(buildResult, getPrevious().getBuildResult()), new UpstreamChangelist.ToChangelistFunction()));
        }

        return upstreamChanges;
    }

    /**
     * @return changelists since {@link #getLastSuccess()} up to and including those affecting the build for this context
     */
    public List<PersistentChangelist> getChangesSinceLastSuccess()
    {
        if (changesSinceLastSuccess == null)
        {
            if (buildResult == null)
            {
                changesSinceLastSuccess = Collections.emptyList();
            }
            else if (buildResult == getLastSuccess())
            {
                changesSinceLastSuccess = getChanges();
            }
            else
            {
                BuildResult since = getLastSuccess();
                long sinceNumber = since == null ? 0 : since.getNumber();
                changesSinceLastSuccess = changelistManager.getChangesForBuild(buildResult, sinceNumber, false);
            }
        }

        return changesSinceLastSuccess;
    }

    /**
     * @return upstream changelists that indirectly affected any build since {@link #getLastSuccess()} up to and
     *         including the build for this context
     */
    public List<PersistentChangelist> getUpstreamChangesSinceLastSuccess()
    {
        if (upstreamChangesSinceLastSuccess == null)
        {
            if (buildResult == null)
            {
                upstreamChangesSinceLastSuccess = Collections.emptyList();
            }
            else if (buildResult == getLastSuccess())
            {
                upstreamChangesSinceLastSuccess = getUpstreamChanges();
            }
            else
            {
                upstreamChangesSinceLastSuccess = newArrayList(transform(changelistManager.getUpstreamChangelists(buildResult, getLastSuccess()), new UpstreamChangelist.ToChangelistFunction()));
            }
        }

        return upstreamChangesSinceLastSuccess;
    }

    /**
     * @return changelists since {@link #getLastHealthy()} up to and including those affecting the build for this context
     */
    public List<PersistentChangelist> getChangesSinceLastHealthy()
    {
        if (changesSinceLastHealthy == null)
        {
            if (buildResult == null)
            {
                changesSinceLastHealthy = Collections.emptyList();
            }
            else if (buildResult == getLastHealthy())
            {
                changesSinceLastHealthy = getChanges();
            }
            else
            {
                BuildResult since = getLastHealthy();
                long sinceNumber = since == null ? 0 : since.getNumber();
                changesSinceLastHealthy = changelistManager.getChangesForBuild(buildResult, sinceNumber, false);
            }
        }

        return changesSinceLastHealthy;
    }

    /**
     * @return upstream changelists that indirectly affected any build since {@link #getLastHealthy()} up to and
     *         including the build for this context
     */
    public List<PersistentChangelist> getUpstreamChangesSinceLastHealthy()
    {
        if (upstreamChangesSinceLastHealthy == null)
        {
            if (buildResult == null)
            {
                upstreamChangesSinceLastHealthy = Collections.emptyList();
            }
            else  if (buildResult == getLastHealthy())
            {
                upstreamChangesSinceLastHealthy = getUpstreamChanges();
            }
            else
            {
                upstreamChangesSinceLastHealthy = newArrayList(transform(changelistManager.getUpstreamChangelists(buildResult, getLastHealthy()), new UpstreamChangelist.ToChangelistFunction()));
            }
        }
        return upstreamChangesSinceLastHealthy;
    }

    /**
     * @return a context for the previous build (with a revision) of the same project
     */
    public NotifyConditionContext getPrevious()
    {
        if (previous == null)
        {
            previous = buildResult == null ? this : new NotifyConditionContext(buildManager.getPreviousBuildResult(buildResult));
        }

        return previous;
    }


    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setChangelistManager(ChangelistManager changelistManager)
    {
        this.changelistManager = changelistManager;
    }
}
