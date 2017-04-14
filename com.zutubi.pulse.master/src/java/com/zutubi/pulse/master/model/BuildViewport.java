/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;

import java.util.LinkedList;
import java.util.List;

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

    /**
     * The build id relative to which this build viewport
     * opperates.  That is, {@link #getNextSuccessfulBuild()} will return
     * the first successful build with a build id higher than this id.
     */
    private long buildId;

    public BuildViewport(long focalBuildId)
    {
        this.buildId = focalBuildId;
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

    /**
     * Get the builds visible in this viewport.  These builds are relative to the build id
     * used to create this view port.  The number of builds returned is defined by the
     * viewport size.
     *
     * @return the visible builds.
     */
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

    /**
     * Get the previous successful build, relative to the focal build
     * id for this view port.
     *
     * @return the previous successful build result, or null if non exists.
     */
    public BuildResult getPreviousSuccessfulBuild()
    {
        return uniqueResult(
                buildResultDao.findByBeforeBuild(buildId, 1, ResultState.getHealthyStates())
        );
    }

    /**
     * Get the previous broken build, relative to the focal build
     * id for this view port.
     *
     * @return the previous broken build result, or null if non exists.
     */
    public BuildResult getPreviousBrokenBuild()
    {
        return uniqueResult(
                buildResultDao.findByBeforeBuild(buildId, 1, ResultState.getBrokenStates())
        );
    }

    /**
     * Get the next successful build, relative to the focal build
     * id for this view port.
     *
     * @return the next successful build result, or null if non exists.
     */
    public BuildResult getNextSuccessfulBuild()
    {
        return uniqueResult(
                buildResultDao.findByAfterBuild(buildId, 1, ResultState.getHealthyStates())
        );
    }

    /**
     * Get the next broken build, relative to the focal build
     * id for this view port.
     *
     * @return the next broken build result, or null if non exists.
     */
    public BuildResult getNextBrokenBuild()
    {
        return uniqueResult(
                buildResultDao.findByAfterBuild(buildId, 1, ResultState.getBrokenStates())
        );
    }

    /**
     * Get the latest build, relative to the focal build
     * id for this view port.
     *
     * @return the latest build result.
     */
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
