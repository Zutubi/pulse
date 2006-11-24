package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.InputStream;

import com.zutubi.pulse.model.Project;

/**
 * <class comment/>
 */
public class ProjectFileObject extends PulseFileObject implements ProjectNode
{
    private final String projectName;

    private long projectId;

    public ProjectFileObject(final FileName name, final String projectName, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.projectName = projectName;
    }

    public PulseFileObject createFile(final FileName fileName) throws Exception
    {
        String path = fileName.getPath();
        if (path.endsWith("builds"))
        {
            return new BuildsFileObject(fileName, projectId, pfs);
        }
        return null;
    }

    protected void doAttach() throws Exception
    {
        Project project = pfs.getProjectManager().getProject(projectName);
        projectId = project.getId();
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

    public Project getProject()
    {
        return null;
    }

    public long getProjectId()
    {
        return 0;
    }
}
