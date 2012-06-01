package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.engine.api.ResultState;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Represents the latest healthy build result for a scope, which may be global or a single project.
 */
public class LatestHealthyBuildFileObject extends LatestInStatesBuildFileObject
{
    public LatestHealthyBuildFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs, ResultState.getHealthyStates());
    }
}
