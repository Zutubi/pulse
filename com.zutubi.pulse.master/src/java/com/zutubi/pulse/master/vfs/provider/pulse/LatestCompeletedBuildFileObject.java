package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.engine.api.ResultState;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Represents the latest completed build result for a scope, which may be global or a single project.
 */
public class LatestCompeletedBuildFileObject extends LatestInStatesBuildFileObject
{
    public LatestCompeletedBuildFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs, ResultState.getCompletedStates());
    }
}
