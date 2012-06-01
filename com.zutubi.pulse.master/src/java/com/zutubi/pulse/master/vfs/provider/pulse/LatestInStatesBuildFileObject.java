package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Abstract base for file objects that represent the latest build in some states for a scope, which
 * may be global or a single project.
 */
public abstract class LatestInStatesBuildFileObject extends AbstractBuildFileObject
{
    private static final Logger LOG = Logger.getLogger(LatestInStatesBuildFileObject.class);

    private ResultState[] inStates;

    public LatestInStatesBuildFileObject(final FileName name, final AbstractFileSystem fs, ResultState... inStates)
    {
        super(name, fs);
        this.inStates = inStates;
    }

    public BuildResult getBuildResult()
    {
        try
        {
            ProjectProvider provider = getAncestor(ProjectProvider.class, true);
            if (provider != null)
            {
                Project project = provider.getProject();
                return buildManager.getLatestBuildResult(project, inStates);
            }
            else
            {
                return buildManager.getLatestBuildResult(inStates);
            }
        }
        catch (FileSystemException e)
        {
            LOG.error(e);
            return null;
        }
    }
}
