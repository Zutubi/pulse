package com.zutubi.pulse.security;

import com.zutubi.prototype.security.Actor;
import com.zutubi.pulse.model.AcegiUser;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.prototype.config.group.ServerPermission;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
import org.acegisecurity.userdetails.UserDetails;

/**
 * <class-comment/>
 */
public class AcegiUtils
{
    private static final AcegiUser systemUser;
    static
    {
        UserConfiguration config = new UserConfiguration();
        config.addDirectAuthority(ServerPermission.ADMINISTER.toString());
        User user = new User();
        user.setConfig(config);
        systemUser = new AcegiUser(user, null);
    }

    public static AcegiUser getSystemUser()
    {
        return systemUser;
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
            if(authentication instanceof AnonymousAuthenticationToken)
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
            if(authentication != null)
            {
                Object details = authentication.getDetails();
                if(details != null && details instanceof BasicAuthenticationDetails)
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
     * given role
     */
    public static boolean userHasRole(String role)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null)
        {
            Object principle = authentication.getPrincipal();
            if(principle instanceof UserDetails)
            {
                AcegiUser details = (AcegiUser) principle;
                return details.getGrantedAuthorities().contains(role);
            }
        }

        return false;
    }
}
