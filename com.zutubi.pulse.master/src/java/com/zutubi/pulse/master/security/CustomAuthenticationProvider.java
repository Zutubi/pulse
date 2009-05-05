package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.security.ldap.LdapManager;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.dao.DaoAuthenticationProvider;
import org.acegisecurity.userdetails.UserDetails;

import java.security.GeneralSecurityException;

/**
 * A custom implementation of {@link DaoAuthenticationProvider}.
 * <p/>
 * This authentication provider is used to provide our custom ldap behaviour
 * <ul>
 * <li>automatically add users authenticated via ldap</li>
 * </ul>
 * <p/>
 * Users created via the auto add functionality do not store the users password locally.
 * Instead, a random hash is created for them each time they manually log in and out, and is
 * used to authenticate there remember me cookies.
 */
public class CustomAuthenticationProvider extends DaoAuthenticationProvider
{
    private static final Logger LOG = Logger.getLogger(CustomAuthenticationProvider.class);

    private UserManager userManager;
    private LdapManager ldapManager;

    /**
     * Implementation of the {@link org.acegisecurity.providers.AuthenticationProvider#authenticate(org.acegisecurity.Authentication)}
     * method.
     *
     * This implementation will attempt to add an unknown user to the system if they are
     * successfully authenticated via ldap.  Passwords associated with users created in this
     * way are random strings used as tokens to locally authenticate the user.  No live passwords
     * are stored for ldap users.
     *
     * @param authentication the authentication request object.
     * @return a fully authenticated object including credentials.
     *
     * @throws AuthenticationException if authentication fails.
     */
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        // Just check for non-existent user auto-adding.
        if (ldapManager.canAutoAdd())
        {
            final UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;

            final String login = token.getName();
            if (TextUtils.stringSet(login))
            {
                // has this user been added yet?.
                final UserConfiguration user = userManager.getUserConfig(login);
                if (user == null)
                {
                    LOG.debug("User '" + login + "' does not exist, asking LDAP manager");
                    AcegiUtils.runAsSystem(new Runnable()
                    {
                        public void run()
                        {
                            tryAutoAdd(login, (String) token.getCredentials());
                        }
                    });
                }
                else if (user.isAuthenticatedViaLdap())
                {
                    AcegiUtils.runAsSystem(new Runnable()
                    {
                        public void run()
                        {
                            setRandomPassword(user);
                        }
                    });
                }
            }
        }

        return super.authenticate(authentication);
    }

    private void tryAutoAdd(String username, String password)
    {
        LOG.debug("Testing for user '" + username + "' via LDAP");

        // We can auto-add the user if they can be authenticated via LDAP.
        UserConfiguration user = ldapManager.authenticate(username, password, true);
        if (user != null)
        {
            LOG.debug("User '" + username + "' found via LDAP, adding.");
            user = userManager.insert(user);

            setRandomPassword(user);
        }
        else
        {
            LOG.debug("User '" + username + "' could not be authenticated via LDAP");
        }
    }

    private void setRandomPassword(UserConfiguration user)
    {
        try
        {
            userManager.setPassword(user, RandomUtils.secureRandomString(10));
        }
        catch (GeneralSecurityException e)
        {
            // fall back to something semi random.
            userManager.setPassword(user, RandomUtils.randomString(10));
        }
    }

    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
    {
        AcegiUser user = (AcegiUser) userDetails;
        if (user.getLdapAuthentication())
        {
            LOG.debug("Authenticating user '" + user.getUsername() + "' via LDAP");
            if (ldapManager.authenticate(authentication.getName(), authentication.getCredentials().toString(), false) == null)
            {
                LOG.debug("Authentication failed for user '" + user.getUsername() + "' via LDAP");
                throw new BadCredentialsException("Bad credentials");
            }

            ldapManager.addLdapRoles(user);
        }
        else
        {
            LOG.debug("Authenticating user '" + user.getUsername() + "' via stored password");
            super.additionalAuthenticationChecks(userDetails, authentication);
        }
    }

    public boolean supports(Class authentication)
    {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setLdapManager(LdapManager ldapManager)
    {
        this.ldapManager = ldapManager;
    }

    public void setUserManager(UserManager userManager)
    {
        setUserDetailsService(userManager);
        this.userManager = userManager;
    }
}
