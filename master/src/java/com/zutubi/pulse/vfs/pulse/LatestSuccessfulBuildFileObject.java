package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Represents the latest successful build result for a scope, which may be
 * global, a single project or a single build specification.
 */
public class LatestSuccessfulBuildFileObject extends AbstractBuildFileObject
{
    private static final Logger LOG = Logger.getLogger(LatestSuccessfulBuildFileObject.class);

    public LatestSuccessfulBuildFileObject(final FileName name, final AbstractFileSystem fs)
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
                    return buildManager.getLatestSuccessfulBuildResult(buildSpecProvider.getBuildSpecification());
                }
                else
                {
                    return buildManager.getLatestSuccessfulBuildResult(project);
                }
            }
            else
            {
                return buildManager.getLatestSuccessfulBuildResult();
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
