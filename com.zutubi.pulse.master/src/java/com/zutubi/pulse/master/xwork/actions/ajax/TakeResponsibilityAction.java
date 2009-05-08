package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.model.User;
import org.acegisecurity.AccessDeniedException;

/**
 * Action allowing a user to take responsibility for a build.
 */
public class TakeResponsibilityAction extends ResponsibilityActionBase
{
    private String comment;

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    @Override
    public SimpleResult doExecute()
    {
        User user = getLoggedInUser();
        if (user == null)
        {
            throw new AccessDeniedException("Anonymous users cannot take responsiblity");
        }

        buildManager.takeResponsibility(getBuildResult(), user, comment);
        return new SimpleResult(true, comment);
    }

}
