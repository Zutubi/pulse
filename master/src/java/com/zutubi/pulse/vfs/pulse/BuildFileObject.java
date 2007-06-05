package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * <class comment/>
 */
public class BuildFileObject extends AbstractPulseFileObject implements BuildResultProvider, AddressableFileObject, ProjectProvider
{
    private final long buildId;

    public BuildFileObject(final FileName name, final long buildId, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.buildId = buildId;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String name = fileName.getBaseName();
        if (name.equals("wc"))
        {
            return objectFactory.buildBean(WorkingCopyContextFileObject.class,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
            );
        }
        else if (name.equals("artifacts"))
        {
            return objectFactory.buildBean(ArtifactsContextFileObject.class,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
            );
        }
        else
        {
            return objectFactory.buildBean(NamedStageFileObject.class,
                    new Class[]{FileName.class, String.class, AbstractFileSystem.class},
                    new Object[]{fileName, name, pfs}
            );
        }
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[]{"wc", "artifacts"};
    }

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath()
    {
        return "/viewBuild.action?id=" + buildId;
    }

    //---( this node is backed by a build result object. )---
    
    public BuildResult getBuildResult()
    {
        return buildManager.getBuildResult(getBuildResultId());
    }

    public long getBuildResultId()
    {
        return buildId;
    }

    public ProjectConfiguration getProjectConfig() throws FileSystemException
    {
        return projectManager.getProjectConfig(getProject().getId());
    }

    public Project getProject()
    {
        return getBuildResult().getProject();
    }

    public long getProjectId()
    {
        return getProject().getId();
    }
}
