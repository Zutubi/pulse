package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.SCMServerUtils;

import java.util.Map;

/**
 * Used for browsing the SCM for tyhe purpose of slecting a file/directory.
 */
public class ViewScmInfoAction extends AbstractBrowseDirAction
{
    private long id;
    private Map<String, String> info;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Map<String, String> getInfo()
    {
        return info;
    }

    public String execute()
    {
        Project project = lookupProject(id);
        if (project == null)
        {
            addActionError("Unknown project [" + project + "]");
            return ERROR;
        }

        SCMServer scm = null;
        try
        {
            scm = project.getScm().createServer();
            info = scm.getServerInfo();

            return SUCCESS;
        }
        catch (Exception e)
        {
            addActionError("Error retrieving SCM info: " + e.getMessage());
            return ERROR;
        }
        finally
        {
            SCMServerUtils.close(scm);
        }
    }
}
