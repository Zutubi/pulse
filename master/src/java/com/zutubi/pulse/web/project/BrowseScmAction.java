package com.zutubi.pulse.web.project;

import com.zutubi.pulse.filesystem.remote.RemoteScmFileSystem;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.SCMServerUtils;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;

import java.util.Map;

/**
 * Used for browsing the SCM for tyhe purpose of slecting a file/directory.
 */
public class BrowseScmAction extends AbstractBrowseDirAction
{
    private long id;
    private String location;
    private boolean selectDir;
    private String elementId;
    private String prefix;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getLocation()
    {
        return location;
    }

    public boolean isSelectDir()
    {
        return selectDir;
    }

    public void setSelectDir(boolean selectDir)
    {
        this.selectDir = selectDir;
    }

    public String getElementId()
    {
        return elementId;
    }

    public void setElementId(String elementId)
    {
        this.elementId = elementId;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public boolean getShowSizes()
    {
        return false;
    }

    public String execute()
    {
        Scm scm;

        if (id != 0)
        {
            Project project = lookupProject(id);
            if (project == null)
            {
                return ERROR;
            }

            scm = project.getScm();
        }
        else
        {
            // Assume project setup
            Map session = ActionContext.getContext().getSession();
            if (!session.containsKey(AddProjectWizard.class.getName()))
            {
                addActionError("Unable to locate SCM configuration from previous step");
                return ERROR;
            }

            AddProjectWizard wizard = (AddProjectWizard) session.get(AddProjectWizard.class.getName());
            scm = wizard.getScm();
        }

        SCMServer server = null;
        try
        {
            server = scm.createServer();
            location = server.getLocation();
            if (TextUtils.stringSet(prefix) && !TextUtils.stringSet(getPath()))
            {
                setPath(prefix);
            }
            return super.execute(new RemoteScmFileSystem(scm));
        }
        catch (Exception e)
        {
            addActionError("Error browsing SCM: " + e.getMessage());
            return ERROR;
        }
        finally
        {
            SCMServerUtils.close(server);
        }
    }
}
