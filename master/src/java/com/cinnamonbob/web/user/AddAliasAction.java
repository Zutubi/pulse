package com.cinnamonbob.web.user;

import com.cinnamonbob.model.User;
import com.opensymphony.util.TextUtils;

/**
 */
public class AddAliasAction extends UserActionSupport
{
    private User user;
    private String alias;

    public String getAlias()
    {
        return alias;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    public String doInput()
    {
        user = getUser();
        if (user == null)
        {
            addUnknownUserActionError();
            return ERROR;
        }

        return INPUT;
    }

    public void validate()
    {
        user = getUser();
        if (user == null)
        {
            addUnknownUserActionError();
            return;
        }

        if (!TextUtils.stringSet(alias))
        {
            addFieldError("alias", "alias is required");
            return;
        }

        if (user.hasAlias(alias))
        {
            addFieldError("alias", "you have already configured an alias with the same value");
        }
    }

    public String execute()
    {
        user.addAlias(alias);
        getUserManager().save(user);
        return SUCCESS;
    }
}
