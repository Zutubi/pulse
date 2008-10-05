package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.Project;

/**
 * An action to change the state of a project (e.g. to pause it);
 */
public class ProjectStateAction extends ProjectActionBase
{
    private boolean pause;

    public boolean isPause()
    {
        return pause;
    }

    public void setPause(boolean pause)
    {
        this.pause = pause;
    }

    public String execute() throws Exception
    {
        Project project = getRequiredProject();

        if (pause)
        {
            projectManager.pauseProject(project);
        }
        else
        {
            projectManager.resumeProject(project);
        }

        return SUCCESS;
    }
}
