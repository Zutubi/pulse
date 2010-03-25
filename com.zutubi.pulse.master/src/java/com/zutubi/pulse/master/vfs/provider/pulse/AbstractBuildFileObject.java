package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 */
public abstract class AbstractBuildFileObject extends AbstractPulseFileObject implements BuildResultProvider, AddressableFileObject, ProjectProvider
{
    public AbstractBuildFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        String name = fileName.getBaseName();
        if (name.equals("wc"))
        {
            accessManager.ensurePermission(ProjectConfigurationActions.ACTION_VIEW_SOURCE, getBuildResult());
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
        return "/browse/projects/" + getProjectConfig().getName() + "/builds/" + getBuildResult().getNumber() + "/";
    }

    public ProjectConfiguration getProjectConfig()
    {
        return projectManager.getProjectConfig(getProjectId(), false);
    }

    public Project getProject()
    {
        return getBuildResult().getProject();
    }

    public long getProjectId()
    {
        return getProject().getId();
    }

    public abstract BuildResult getBuildResult();
    public abstract long getBuildResultId();
}
