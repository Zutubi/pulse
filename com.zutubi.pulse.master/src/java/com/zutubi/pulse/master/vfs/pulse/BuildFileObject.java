package com.zutubi.pulse.master.vfs.pulse;

import com.zutubi.pulse.master.model.BuildResult;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Represents a build where the ID is specified directly.
 */
public class BuildFileObject extends AbstractBuildFileObject
{
    private final long buildId;

    public BuildFileObject(final FileName name, final long buildId, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.buildId = buildId;
    }

    public long getBuildResultId()
    {
        return buildId;
    }

    public BuildResult getBuildResult()
    {
        return buildManager.getBuildResult(buildId);
    }
}
