package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.Project;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <class comment/>
 */
public class ProjectFileObject extends AbstractPulseFileObject implements ProjectProvider, AddressableFileObject
{
    private static final Map<String, Class> nodesDefinitions = new HashMap<String, Class>();
    {
        nodesDefinitions.put("builds", BuildsFileObject.class);
        nodesDefinitions.put("latest", LatestBuildFileObject.class);
        nodesDefinitions.put("successful", LatestSuccessfulBuildFileObject.class);
        nodesDefinitions.put("latestsuccessful", LatestSuccessfulBuildFileObject.class);
        nodesDefinitions.put("specs", BuildSpecificationsFileObject.class);
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
            Class clazz = nodesDefinitions.get(name);
            return objectFactory.buildBean(clazz,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
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
        Set<String> children = nodesDefinitions.keySet();
        return children.toArray(new String[children.size()]);
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

    public String getUrlPath()
    {
        return "/currentBuild.action?id=" + getProjectId();
    }
}
