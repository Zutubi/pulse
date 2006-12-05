package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.BuildResult;

/**
 * A provider interface that indicates the current node represents a build result instance.
 *
 * @see com.zutubi.pulse.model.BuildResult
 */
public interface BuildResultProvider
{
    BuildResult getBuildResult();

    long getBuildResultId();
}
