package com.zutubi.pulse.master.xwork.actions;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.security.SecurityUtils;

/**
 * The default action controls the page presented to the user when they first arrive at the
 * application.
 *
 * It handles a number of things.
 * 1) if the system has not been setup, then it will direct the user to the setup wizard.
 * 2) directs the user to their default page, either the welcome page or the dashboard.
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
     * The browse view result. See xwork config for details.
     */
    public static final String BROWSE_ACTION = "browse";

    /**
     * The setup result. See the xwork config for details.
     */
    private static final String SETUP_ADMIN = "setupAdmin";

    public String execute()
    {
        if (userManager.getUserCount() == 0)
        {
            return SETUP_ADMIN;
        }

        String login = SecurityUtils.getLoggedInUsername();
        if(login == null)
        {
            return BROWSE_ACTION;
        }
        else
        {
            User user = userManager.getUser(login);
            if (user == null)
            {
                return BROWSE_ACTION;
            }
            else
            {
                return user.getPreferences().getDefaultAction();
            }
        }
    }
}
