package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

public class PreferencesAction extends ActionSupport
{
    public String execute() throws Exception
    {
        String login = SecurityUtils.getLoggedInUsername();
        if (login == null)
        {
            return "guest";
        }

        return SUCCESS;
    }
}
