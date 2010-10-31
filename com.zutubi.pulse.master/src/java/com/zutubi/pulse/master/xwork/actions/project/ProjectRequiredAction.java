package com.zutubi.pulse.master.xwork.actions.project;

/**
 * Generic project action that just ensures the project exists.
 */
public class ProjectRequiredAction extends ProjectActionBase
{
    public String execute()
    {
        getRequiredProject();
        return SUCCESS;
    }
}
