package com.zutubi.pulse.master.model;

import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.core.engine.api.ResultState;

import java.util.List;
import java.util.LinkedList;

/**
 * A build viewport allows random access to a list of builds, providing
 * access to a specified build as well as the builds around it.
 *
 * The primary purpose of this is to provide contextual navigation to the UI.
 *
 * The viewport size defines the maximum number of builds 'visible' in the
 * viewport {@link #getVisibleBuilds}, and {@link #getNextBrokenBuild()} etc
 * allow access to nearby builds that may not be visible within the viewport. 
 */
public class BuildViewport
{
    public static final int DEFAULT_VIEWPORT_SIZE = 5;

    private BuildResultDao buildResultDao;

    private int viewportSize = DEFAULT_VIEWPORT_SIZE;

    private long buildId;

    public BuildViewport(long buildId)
    {
        this.buildId = buildId;
    }

    /**
     * Set the size of the viewport.  That is, the maximum number of visible builds.
     *
     * @param viewportSize  the viewport size.
     *
     * @see #getVisibleBuilds() 
     */
    public void setViewportSize(int viewportSize)
    {
        this.viewportSize = viewportSize;
    }

    public List<BuildResult> getVisibleBuilds()
    {
        List<BuildResult> viewport = new LinkedList<BuildResult>();

        BuildResult result = buildResultDao.findById(buildId);
        if (result == null)
        {
            return viewport;
        }
        
        List<BuildResult> resultsBefore = buildResultDao.findByBeforeBuild(buildId, viewportSize);
        List<BuildResult> resultsAfter = buildResultDao.findByAfterBuild(buildId, viewportSize);

        int lookAheadSize = (viewportSize - 1) / 2;
        int lookBehindSize = lookAheadSize + (viewportSize - 1) % 2;

        if (resultsBefore.size() < lookBehindSize)
        {
            lookBehindSize = resultsBefore.size();
            lookAheadSize = (viewportSize - 1) - lookBehindSize;
            if (resultsAfter.size() < lookAheadSize)
            {
                lookAheadSize = resultsAfter.size();
            }
        }
        else if (resultsAfter.size() < lookAheadSize)
        {
            lookAheadSize = resultsAfter.size();
            lookBehindSize = (viewportSize - 1) - lookAheadSize;
            if (resultsBefore.size() < lookBehindSize)
            {
                lookBehindSize = resultsBefore.size();
            }
        }

        viewport.addAll(resultsBefore.subList(resultsBefore.size() - lookBehindSize, resultsBefore.size()));
        viewport.add(result);
        viewport.addAll(resultsAfter.subList(0, lookAheadSize));

        return viewport;
    }

    public BuildResult getPreviousSuccessfulBuild()
    {
        return uniqueResult(
                buildResultDao.findByBeforeBuild(buildId, 1, ResultState.SUCCESS)
        );
    }

    public BuildResult getPreviousBrokenBuild()
    {
        return uniqueResult(
                buildResultDao.findByBeforeBuild(buildId, 1, ResultState.getBrokenStates())
        );
    }

    public BuildResult getNextSuccessfulBuild()
    {
        return uniqueResult(
                buildResultDao.findByAfterBuild(buildId, 1, ResultState.SUCCESS)
        );
    }

    public BuildResult getNextBrokenBuild()
    {
        return uniqueResult(
                buildResultDao.findByAfterBuild(buildId, 1, ResultState.getBrokenStates())
        );
    }

    public BuildResult getLatestBuild()
    {
        return buildResultDao.findByLatestBuild(buildId);
    }

    private BuildResult uniqueResult(List<BuildResult> results)
    {
        if (results.size() > 1)
        {
            throw new IllegalArgumentException("Expected at most 1 item in the list.");
        }
        if (results.size() > 0)
        {
            return results.get(0);
        }
        return null;
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }
}
