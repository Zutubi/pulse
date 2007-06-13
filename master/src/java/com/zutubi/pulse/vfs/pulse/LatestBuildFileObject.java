package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Represents the latest build result for a scope, which may be global, a
 * single project or a single build specification.
 */
public class LatestBuildFileObject extends AbstractBuildFileObject
{
    private static final Logger LOG = Logger.getLogger(LatestBuildFileObject.class);

    public LatestBuildFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public BuildResult getBuildResult()
    {
        try
        {
            ProjectProvider provider = getAncestor(ProjectProvider.class);
            if (provider != null)
            {
                Project project = provider.getProject();

                BuildSpecificationProvider buildSpecProvider = getAncestor(BuildSpecificationProvider.class);
                if (buildSpecProvider != null)
                {
                    return buildManager.getLatestBuildResult(buildSpecProvider.getBuildSpecification());
                }
                else
                {
                    return buildManager.getLatestBuildResult(project);
                }
            }
            else
            {
                return buildManager.getLatestBuildResult();
            }
        }
        catch (FileSystemException e)
        {
            LOG.error(e);
            return null;
        }
    }

    public long getBuildResultId()
    {
        BuildResult result = getBuildResult();
        if (result != null)
        {
            return result.getId();
        }
        return -1;
    }
}
