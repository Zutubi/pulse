package com.zutubi.pulse.web.user;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.web.ActionSupport;

import java.util.Arrays;

/**
 *
 * 
 */
public class UserActionSupport extends ActionSupport
{
    private MasterConfigurationManager configurationManager;

    /**
     * static long indicating that a value has not been specified.
     */
    public static final long NONE_SPECIFIED = -1;

    /**
     * User id.
     */
    private long userId = NONE_SPECIFIED;

    /**
     * User login.
     */
    private String userLogin = null;

    private User user = null;

    /**
     *
     * @return user id, or -1 if none specified.
     */
    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long user)
    {
        this.userId = user;
    }

    public String getUserLogin()
    {
        return userLogin;
    }

    public void setUserLogin(String userLogin)
    {
        this.userLogin = userLogin;
    }

    public User getUser()
    {
        if(user != null)
        {
            return user;
        }
        
        if (userId != NONE_SPECIFIED)
        {
            return getUser(userId);
        }
        else if (TextUtils.stringSet(userLogin))
        {
            return getUser(userLogin);
        }

        Object principle = getPrinciple();
        if(principle != null)
        {
            return getUser((String) principle);
        }
        return null;
    }

    protected User getUser(String userLogin)
    {
        return userManager.getUser(userLogin);
    }

    protected User getUser(long userId)
    {
        return userManager.getUser(userId);
    }

    protected void addUnknownUserActionError()
    {
        if (userId != NONE_SPECIFIED)
        {
            addActionError(getText("user.unknown", Arrays.asList(userId)));
        }
        else if (TextUtils.stringSet(userLogin))
        {
            addActionError(getText("user.unknown", Arrays.asList(userLogin)));
        }
    }

    protected void addUnknownUserFieldError()
    {
        if (userId != NONE_SPECIFIED)
        {
            addFieldError("userId", "Unknown user '" + userId + "'");
        }
        else if (TextUtils.stringSet(userLogin))
        {
            addFieldError("userLogin", "Unknown user '" + userLogin + "'");
        }
    }

    public MasterConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
