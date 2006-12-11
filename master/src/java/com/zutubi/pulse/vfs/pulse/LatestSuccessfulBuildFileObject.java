package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.Map;
import java.util.HashMap;

/**
 * <class comment/>
 */
public class LatestSuccessfulBuildFileObject extends AbstractPulseFileObject implements AddressableFileObject, BuildResultProvider
{
    private static final Map<String, Class> nodesDefinitions = new HashMap<String, Class>();
    {
        // setup the default root node definitions.
        nodesDefinitions.put("artifacts", NamedArtifactsFileObject.class);
    }

    private static final Logger LOG = Logger.getLogger(LatestSuccessfulBuildFileObject.class);

    public LatestSuccessfulBuildFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String name = fileName.getBaseName();
        if (nodesDefinitions.containsKey(name))
        {
            Class clazz = nodesDefinitions.get(name);
            return objectFactory.buildBean(clazz,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
            );
        }
        return null;
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.IMAGINARY;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[0];
    }

    public String getUrlPath()
    {
        return "/viewBuild.action?id=" + getBuildResultId();
    }

    public BuildResult getBuildResult()
    {
        try
        {
            ProjectProvider provider = (ProjectProvider) getAncestor(ProjectProvider.class);
            if (provider != null)
            {
                Project project = provider.getProject();

                BuildSpecificationProvider buildSpecProvider = (BuildSpecificationProvider) getAncestor(BuildSpecificationProvider.class);
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
