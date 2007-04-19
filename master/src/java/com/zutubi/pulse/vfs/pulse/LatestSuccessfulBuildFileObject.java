package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.HashMap;
import java.util.Map;

/**
 * <class comment/>
 */
public class LatestSuccessfulBuildFileObject extends AbstractPulseFileObject implements AddressableFileObject, BuildResultProvider
{
    private static final Map<String, Class<? extends AbstractPulseFileObject>> nodesDefinitions = new HashMap<String, Class<? extends AbstractPulseFileObject>>();
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
            Class<? extends AbstractPulseFileObject> clazz = nodesDefinitions.get(name);
            return objectFactory.buildBean(clazz,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
            );
        }
        
        return objectFactory.buildBean(NamedStageFileObject.class,
                new Class[]{FileName.class, String.class, AbstractFileSystem.class},
                new Object[]{fileName, name, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.IMAGINARY;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[0];
    }

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath()
    {
        return "/viewBuild.action?id=" + getBuildResultId();
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
