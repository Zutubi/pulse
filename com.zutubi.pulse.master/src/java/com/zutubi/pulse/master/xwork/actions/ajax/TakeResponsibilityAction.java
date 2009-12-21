package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.model.User;

/**
 * Action allowing a user to take responsibility for a project.
 */
public class TakeResponsibilityAction extends ResponsibilityActionBase
{
    private String message;

    public void setMessage(String message)
    {
        this.message = message;
    }

    @Override
    public SimpleResult doExecute()
    {
        User user = getLoggedInUser();
        projectManager.takeResponsibility(getProject(), user, message);
        return new SimpleResult(true, message);
    }
}
