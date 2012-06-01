package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.engine.api.ResultState;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Represents the latest successful build result for a scope, which may be
 * global or a single project.
 */
public class LatestSuccessfulBuildFileObject extends LatestInStatesBuildFileObject
{
    public LatestSuccessfulBuildFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs, ResultState.SUCCESS);
    }
}
