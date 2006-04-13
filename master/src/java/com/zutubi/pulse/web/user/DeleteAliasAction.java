/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.User;

/**
 */
public class DeleteAliasAction extends UserActionSupport
{
    /**
     * One based index of alias to delete.
     */
    private int index;

    public void setIndex(int index)
    {
        this.index = index;
    }

    public String execute()
    {
        User user = getUser();
        if (user == null)
        {
            addUnknownUserActionError();
            return ERROR;
        }

        user.removeAlias(index - 1);
        getUserManager().save(user);
        return SUCCESS;
    }
}
