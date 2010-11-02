package com.zutubi.pulse.master.xwork.actions.project;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.pulse.master.model.User;

/**
 * Action for viewing project dependencies.
 */
public class ProjectDependenciesAction extends ProjectActionBase
{
    public static final String ANONYMOUS_MODE_KEY = "pulse.anonymousUserDependencyTransientMode";

    private String transitiveMode;

    public String getTransitiveMode()
    {
        return transitiveMode;
    }

    public String execute() throws Exception
    {
        getRequiredProject();
        transitiveMode = lookupTransitiveMode().name();
        return SUCCESS;
    }

    protected ProjectDependencyGraphBuilder.TransitiveMode lookupTransitiveMode()
    {
        User user = getLoggedInUser();
        ProjectDependencyGraphBuilder.TransitiveMode mode;
        if (user == null)
        {
            mode = (ProjectDependencyGraphBuilder.TransitiveMode) ActionContext.getContext().getSession().get(ANONYMOUS_MODE_KEY);
            if (mode == null)
            {
                mode = ProjectDependencyGraphBuilder.TransitiveMode.FULL;
            }
        }
        else
        {
            mode = user.getPreferences().getDependencyTransitiveMode();
        }

        return mode;
    }
}
