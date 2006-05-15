/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.security.AcegiUtils;

/**
 * <class-comment/>
 */
public class PreferencesAction extends UserActionSupport
{
    public String getRefreshInterval()
    {
        long refreshInterval = getUser().getRefreshInterval();
        if (refreshInterval == User.REFRESH_DISABLED)
        {
            return "never";
        }
        else
        {
            return "every " + refreshInterval + " seconds";
        }
    }

    public String doInput() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        if (login == null)
        {
            return "guest";
        }

        setUserLogin(login);

        // load the user from the db.
        User user = getUser();
        if (user == null)
        {
            addUnknownUserActionError();
            return ERROR;
        }
        return super.doInput();
    }

    public String execute() throws Exception
    {
        String result = doInput();
        if (result.equals(INPUT))
        {
            return SUCCESS;
        }
        return result;
    }
}
