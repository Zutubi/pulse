package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.InputStream;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;

/**
 * <class comment/>
 */
public class ProjectFileObject extends AbstractPulseFileObject implements ProjectNode
{
    private ProjectManager projectManager;

    private String displayName;

    private long projectId;

    public ProjectFileObject(final FileName name, final long projectId, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.projectId = projectId;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String path = fileName.getPath();
        if (path.endsWith("builds"))
        {
            return objectFactory.buildBean(BuildsFileObject.class,
                    new Class[]{FileName.class, Long.TYPE, AbstractFileSystem.class},
                    new Object[]{fileName, projectId, pfs}
            );
        }
        return null;
    }

    protected void doAttach() throws Exception
    {
        Project project = getProject();
        displayName = project.getName();
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[]{"builds"};
    }

    protected long doGetContentSize() throws Exception
    {
        return 0;
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return null;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public Project getProject()
    {
        return projectManager.getProject(projectId);
    }

    public long getProjectId()
    {
        return projectId;
    }

    /**
     * Required resource.
     *
     * @param projectManager instance.
     */
    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
