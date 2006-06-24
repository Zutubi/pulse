package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.PostBuildAction;
import com.zutubi.pulse.model.RunExecutablePostBuildAction;

/**
 */
public class EditExeActionAction extends AbstractEditPostBuildActionAction
{
    // Create it so webwork doesn't try to
    private RunExecutablePostBuildAction action = new RunExecutablePostBuildAction();

    public void prepare() throws Exception
    {
        super.prepare();
        if(hasErrors())
        {
            return;
        }

        PostBuildAction a = getProject().getPostBuildAction(getId());
        if (a == null)
        {
            addActionError("Unknown post build action [" + getId() + "]");
            return;
        }

        if (!(a instanceof RunExecutablePostBuildAction))
        {
            addActionError("Invalid post build action type '" + a.getType() + "'");
            return;
        }

        action = (RunExecutablePostBuildAction) a;
    }

    public void validate()
    {
        super.validate();

        try
        {
            RunExecutablePostBuildAction.validateArguments(action.getArguments());
        }
        catch (Exception e)
        {
            addFieldError("postBuildAction.arguments", e.getMessage());
        }
    }

    public RunExecutablePostBuildAction getPostBuildAction()
    {
        return action;
    }
}
