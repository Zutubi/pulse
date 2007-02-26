package com.zutubi.prototype.webwork;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;

/**
 *
 *
 */
public class CreateChildProjectAction extends ActionSupport
{
    private ProjectManager projectManager;

    private long parent;

    public void setParent(long parent)
    {
        this.parent = parent;
    }

    public String execute() throws Exception
    {
        Project project = new Project();


        return super.execute();
    }
}
