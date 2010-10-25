package com.zutubi.pulse.master.xwork.actions.project;

/**
 * Action to display project home page.  Just ensures the project exists, the
 * real work is done with an AJAX request to {@link ProjectHomeDataAction}.
 */
public class ProjectHomeAction extends ProjectActionBase
{
    public String execute()
    {
        getRequiredProject();
        return SUCCESS;
    }
}
