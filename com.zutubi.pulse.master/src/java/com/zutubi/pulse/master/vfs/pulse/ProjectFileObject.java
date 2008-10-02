package com.zutubi.pulse.master.vfs.pulse;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.webwork.mapping.Urls;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 */
public class ProjectFileObject extends AbstractPulseFileObject implements ProjectProvider, AddressableFileObject
{
    private static final Map<String, Class<? extends AbstractPulseFileObject>> nodesDefinitions = new HashMap<String, Class<? extends AbstractPulseFileObject>>();
    {
        nodesDefinitions.put("builds", BuildsFileObject.class);
        nodesDefinitions.put("latest", LatestBuildFileObject.class);
        nodesDefinitions.put("successful", LatestSuccessfulBuildFileObject.class);
        nodesDefinitions.put("latestsuccessful", LatestSuccessfulBuildFileObject.class);
        nodesDefinitions.put("scm", ScmRootFileObject.class);
    }
    private String displayName;

    private long projectId;

    public ProjectFileObject(final FileName name, final long projectId, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.projectId = projectId;
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
        return null;
    }

    protected void doAttach() throws Exception
    {
        displayName = getProjectConfig().getName();
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        Set<String> children = nodesDefinitions.keySet();
        return children.toArray(new String[children.size()]);
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public ProjectConfiguration getProjectConfig() throws FileSystemException
    {
        return projectManager.getProjectConfig(projectId, false);
    }

    public Project getProject()
    {
        return projectManager.getProject(projectId, false);
    }

    public long getProjectId()
    {
        return projectId;
    }

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath()
    {
        return new Urls("").project(getProject());
    }
}
