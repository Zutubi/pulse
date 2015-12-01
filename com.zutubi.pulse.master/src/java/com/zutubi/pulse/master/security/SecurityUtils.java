package com.zutubi.pulse.master.security;

import com.zutubi.pulse.core.dependency.ivy.AuthenticatedAction;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.tove.security.Actor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import javax.servlet.http.HttpSession;
import java.util.concurrent.Callable;

/**
 * Utility functions for the security system.
 */
public class SecurityUtils
{
    private static final Principle systemUser;
    private static final Principle repositoryUser;

    static
    {
        UserConfiguration systemUserConfig = new UserConfiguration();
        systemUserConfig.addDirectAuthority(ServerPermission.ADMINISTER.toString());
        systemUserConfig.setLogin("<system>");
        User user = new User();
        user.setConfig(systemUserConfig);
        systemUser = new Principle(user, null);

        UserConfiguration repositoryUserConfig = new UserConfiguration();
        repositoryUserConfig.setName(AuthenticatedAction.USER);
        repositoryUserConfig.setLogin(AuthenticatedAction.USER);
        repositoryUserConfig.addDirectAuthority(ServerPermission.ADMINISTER.toString());
        repositoryUser = new Principle(repositoryUserConfig, null);
    }

    public static Principle getSystemUser()
    {
        return systemUser;
    }

    public static Principle getRepositoryUser()
    {
        return repositoryUser;
    }

    /**
     * Logs the current thread in as the system user, with privileges to do
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
    public static void loginAs(Principle targetUser)
    {
        UsernamePasswordAuthenticationToken targetUserRequest =
                new UsernamePasswordAuthenticationToken(targetUser, targetUser.getPassword(), targetUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(targetUserRequest);
    }

    /**
     * Hackish utility to force storage of the current authentication in the session. Under normal
     * operation this is handled by Spring Security filters, but this can be useful when filters are
     * not available (e.g. during setup to log in as admin).
     *
     * @param session session map to save into
     */
    @SuppressWarnings("unchecked")
    public static void saveAuthenticationInSession(HttpSession session)
    {
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    }

    /**
     * Runs the given task as the given user, then restores the original
     * user.
     *
     * @param user     user to run the task as
     * @param runnable the task to run
     * @see #runAsSystem(Runnable)
     */
    public static void runAsUser(Principle user, Runnable runnable)
    {
        // Note: replacing the authentication itself is unsafe in the presence
        // of the SecurityContextPersistenceFilter, which shares contexts
        // among threads from the same session.
        SecurityContext existingContext = SecurityContextHolder.getContext();
        try
        {
            SecurityContextHolder.setContext(new SecurityContextImpl());
            loginAs(user);
            runnable.run();
        }
        finally
        {
            SecurityContextHolder.setContext(existingContext);
        }
    }

    /**
     * Runs the given task as the system user, then restores the original
     * user.
     *
     * @param runnable the task to run
     * @see #callAsUser(Principle, java.util.concurrent.Callable)
     */
    public static void runAsSystem(Runnable runnable)
    {
        runAsUser(systemUser, runnable);
    }

    public static <V> V callAsUser(Principle user, Callable<V> callable) throws Exception
    {
        // Note: replacing the authentication itself is unsafe in the presence
        // of the SecurityContextPersistenceFilter, which shares contexts
        // among threads from the same session.
        SecurityContext existingContext = SecurityContextHolder.getContext();
        try
        {
            SecurityContextHolder.setContext(new SecurityContextImpl());
            loginAs(user);
            return callable.call();
        }
        finally
        {
            SecurityContextHolder.setContext(existingContext);
        }
    }

    public static <V> V callAsSystem(Callable<V> callable) throws Exception
    {
        return callAsUser(systemUser, callable);
    }

    public static <V> Callable<V> callableAsSystem(final Callable<V> callable)
    {
        return new Callable<V>()
        {
            public V call() throws Exception
            {
                return callAsSystem(callable);
            }
        };
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
                if (principle instanceof Principle)
                {
                    return ((Principle) principle);
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
                Principle details = (Principle) principle;
                return details.getGrantedAuthorities().contains(role);
            }
        }

        return false;
    }
}
