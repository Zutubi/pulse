package com.cinnamonbob.servlet;

import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.model.User;
import com.cinnamonbob.bootstrap.ComponentContext;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import java.io.IOException;
import java.text.MessageFormat;

/**
 *
 */
public class ProfileServlet extends HttpServlet
{
    // This needs review. Where should this servlet direct a user? Can you view another users
    // profile?

    private static final String DISPLAY_PROFILE_PATH = "/admin/viewUser.action?id={0}";

    private UserManager userManager;

    public UserManager getUserManager()
    {
        if (userManager == null)
        {
            // a) servlet is not autowired.
            // b) when servlet is initialised, the userManager is not available.
            userManager = (UserManager) ComponentContext.getBean("userManager");
        }
        return userManager;
    }

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
    {
        String requestPath = httpServletRequest.getPathInfo();
        if (requestPath == null)
        {
            httpServletResponse.sendError(404);
            return;
        }

        if (requestPath.startsWith("/"))
        {
            requestPath = requestPath.substring(1);
        }

        String loginName = requestPath;

        // check the validity of the path.
        User user = getUserManager().getUser(loginName);
        if (user == null)
        {
            httpServletResponse.sendError(404);
            return;
        }

        String pathToForward = MessageFormat.format(DISPLAY_PROFILE_PATH, new String[]{String.valueOf(user.getId())});

        httpServletRequest.getRequestDispatcher(pathToForward).forward(httpServletRequest, httpServletResponse);
    }

}
