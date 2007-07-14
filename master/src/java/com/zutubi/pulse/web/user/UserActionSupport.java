package com.zutubi.pulse.web.user;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.UserLoginComparator;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.web.admin.user.ViewUsersAction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * 
 */
public class UserActionSupport extends ActionSupport
{
    /**
     * Useful resources commonly used by User related Actions.
     */
    private UserManager userManager;

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
     * Required resource.
     *
     * @param userManager
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public UserManager getUserManager()
    {
        return userManager;
    }

    /**
     *
     * @return user id, or -1 if none specified.
     */
    public long getUserId()
    {
        return userId;
    }

    /**
     * Set the user id
     *
     * @param user
     */
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

    public User getAdminUser()
    {
        return userManager.getUser(getAdminLogin());
    }

    public boolean isAdminUser(User user)
    {
        return user.getLogin().equals(getAdminLogin());
    }

    private String getAdminLogin()
    {
        return getConfigurationManager().getAppConfig().getAdminLogin();
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

    protected int getUserStartPage(User user)
    {
        List<User> all = getUserManager().getAllUsers();
        Collections.sort(all, new UserLoginComparator());
        int index = all.indexOf(user);
        return index / ViewUsersAction.USERS_PER_PAGE;
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
