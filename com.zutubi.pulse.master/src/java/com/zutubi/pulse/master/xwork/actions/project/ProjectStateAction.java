package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.EnumUtils;

/**
 * An action to change the state of a project (e.g. to pause it);
 */
public class ProjectStateAction extends ProjectActionBase
{
    private String transition;

    public void setTransition(String transition)
    {
        this.transition = transition;
    }

    public String execute() throws Exception
    {
        Project project = getRequiredProject();
        Project.Transition transition = EnumUtils.fromPrettyString(Project.Transition.class, this.transition);
        if (isTransitionAllowed(transition))
        {
            projectManager.makeStateTransition(project.getId(), transition);
        }
        
        return SUCCESS;
    }

    private boolean isTransitionAllowed(Project.Transition transition)
    {
        switch (transition)
        {
            case INITIALISE:
            case PAUSE:
            case RESUME:
                return true;
            default:
                return false;
        }
    }
}
