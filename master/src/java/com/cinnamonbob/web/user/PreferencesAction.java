package com.cinnamonbob.web.user;

import com.cinnamonbob.model.User;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.security.AcegiUtils;

/**
 * <class-comment/>
 */
public class PreferencesAction extends UserActionSupport
{
    public String doInput() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        if (login == null)
        {
            return ERROR;
        }

        setUserLogin(login);

        // load the user from the db.
        User user = getUser();
        if (user == null)
        {
            addUnknownUserError();
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
