package com.zutubi.pulse.web;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.security.AcegiUtils;

/**
 * The default action controls the page presented to the user when they first arrive at the
 * application.
 *
 * It handles a number of things.
 * 1) if the system has not been setup, then it will direct the user to the setup wizard.
 * 2) directs the user to there default page, either the welcome page or the dashboard.
 */
public class DefaultAction extends ActionSupport
{
    /**
     * The welcome result. See the xwork config for details.
     */
    public static final String WELCOME_ACTION = "welcome";

    /**
     * The dashboard result. See the xwork config for details.
     */
    public static final String DASHBOARD_ACTION = "dashboard";

    /**
     * The projects dashboard result. See xwork config for details.
     */
    public static final String PROJECT_DASHBOARD_ACTION = "projects";

    /**
     * The setup result. See the xwork config for details.
     */
    private static final String SETUP_ADMIN = "setupAdmin";

    private UserManager userManager;

    public String execute()
    {
        if (userManager.getUserCount() == 0)
        {
            return SETUP_ADMIN;
        }

        String login = AcegiUtils.getLoggedInUser();
        if(login == null)
        {
            return PROJECT_DASHBOARD_ACTION;
        }
        else
        {
            User user = userManager.getUser(login);
            return user.getDefaultAction();
        }
    }

    /**
     * Required resource.
     *
     * @param userManager
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
