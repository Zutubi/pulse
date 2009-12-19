package com.zutubi.pulse.master.security;

import com.zutubi.pulse.core.dependency.ivy.AuthenticatedAction;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.tove.security.Actor;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
import org.acegisecurity.userdetails.UserDetails;

/**
 * Utility functions for the security system.
 */
public class AcegiUtils
{
    private static final AcegiUser systemUser;
    private static final AcegiUser repositoryUser;
    static
    {
        UserConfiguration systemUserConfig = new UserConfiguration();
        systemUserConfig.addDirectAuthority(ServerPermission.ADMINISTER.toString());
        systemUserConfig.setLogin("<system>");
        User user = new User();
        user.setConfig(systemUserConfig);
        systemUser = new AcegiUser(user, null);

        UserConfiguration repositoryUserConfig = new UserConfiguration();
        repositoryUserConfig.setName(AuthenticatedAction.USER);
        repositoryUserConfig.setLogin(AuthenticatedAction.USER);
        repositoryUserConfig.addDirectAuthority(ServerPermission.ADMINISTER.toString());
        repositoryUser = new AcegiUser(repositoryUserConfig, null);
    }

    public static AcegiUser getSystemUser()
    {
        return systemUser;
    }

    public static AcegiUser getRepositoryUser()
    {
        return repositoryUser;
    }

    /**
     * Logs the current thread in as the system user, with priveleges to do
     * everything.
     */
    public static void loginAsSystem()
    {
        loginAs(systemUser);
    }

    /**
     * A utility method to 'log in' the specified user. After this call, all authentication
     * requests will be made against the new user.
     *
     * @param targetUser user to log in as
     */
    public static void loginAs(AcegiUser targetUser)
    {
        UsernamePasswordAuthenticationToken targetUserRequest =
                new UsernamePasswordAuthenticationToken(targetUser, targetUser.getPassword(), targetUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(targetUserRequest);
    }

    /**
     * Runs the given task as the given user, then restores the original
     * user.
     *
     * @param user     user to run the task as
     * @param runnable the task to run
     * @see #runAsSystem(Runnable)
     */
    public static void runAsUser(AcegiUser user, Runnable runnable)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try
        {
            loginAs(user);
            runnable.run();
        }
        finally
        {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    /**
     * Runs the given task as the system user, then restores the original
     * user.
     *
     * @param runnable the task to run
     * @see #runAsUser(AcegiUser,Runnable)
     */
    public static void runAsSystem(Runnable runnable)
    {
        runAsUser(systemUser, runnable);
    }

    public static void logout()
    {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    /**
     * Returns the username for the currently-logged-in User.
     *
     * @return the logged in user's login name, or null if there is no
     *         logged in user
     */
    public static String getLoggedInUsername()
    {
        Actor actor = getLoggedInUser();
        return actor == null ? null : actor.getUsername();
    }

    public static Actor getLoggedInUser()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null)
        {
            if (authentication instanceof AnonymousAuthenticationToken)
            {
                AnonymousAuthenticationToken token = (AnonymousAuthenticationToken) authentication;
                return new AnonymousActor(token);
            }
            else
            {
                Object principle = authentication.getPrincipal();
                if (principle instanceof AcegiUser)
                {
                    return ((AcegiUser) principle);
                }
            }
        }
        return null;
    }

    /**
     * Returns true iff a user is currently logged in and is able to log out.
     * An example of a user that cannot logout is one authenticated via HTTP
     * basic authentication: in this case their client is logging them in, so
     * a logout via pulse will have no effect (CIB-545).
     *
     * @return true iff there is a logged in user who can log out
     */
    public static boolean canLogout()
    {
        if (getLoggedInUsername() != null)
        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null)
            {
                Object details = authentication.getDetails();
                if (details != null && details instanceof BasicAuthenticationDetails)
                {
                    return false;
                }
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Tests if the logged in user has been granted the given role.
     *
     * @param role the role to test for
     * @return true iff there is a logged in user who has been granted the
     *         given role
     */
    public static boolean userHasRole(String role)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null)
        {
            Object principle = authentication.getPrincipal();
            if (principle instanceof UserDetails)
            {
                AcegiUser details = (AcegiUser) principle;
                return details.getGrantedAuthorities().contains(role);
            }
        }

        return false;
    }
}
