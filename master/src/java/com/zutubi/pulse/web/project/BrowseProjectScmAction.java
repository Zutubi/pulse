/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.filesystem.remote.RemoteScmFileSystem;
import com.zutubi.pulse.model.Project;
import org.acegisecurity.acl.AclManager;

/**
 */
public class BrowseProjectScmAction extends AbstractBrowseDirAction
{
    private long id;
    private Project project;
    private String location;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Project getProject()
    {
        return project;
    }

    public String getLocation()
    {
        return location;
    }

    public boolean getShowSizes()
    {
        return false;
    }

    public String execute()
    {
        project = getProjectManager().getProject(id);
        if (project == null)
        {
            addActionError("Unknown project [" + id + "]");
            return ERROR;
        }

        getProjectManager().checkWrite(project);

        try
        {
            location = project.getScm().createServer().getLocation();
            return super.execute(new RemoteScmFileSystem(project.getScm()));
        }
        catch (Exception e)
        {
            addActionError("Error browsing SCM: " + e.getMessage());
            return ERROR;
        }
    }
}
