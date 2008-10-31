package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.model.BuildResult;
import org.apache.commons.vfs.FileSystemException;

/**
 * A provider interface that indicates the current node represents a build result instance.
 *
 * @see com.zutubi.pulse.master.model.BuildResult
 */
public interface BuildResultProvider
{
    BuildResult getBuildResult() throws FileSystemException;

    long getBuildResultId() throws FileSystemException;
}
