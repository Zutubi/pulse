package com.zutubi.pulse.master.security;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.find;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.security.ldap.LdapManager;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

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
     * Implementation of the {@link org.springframework.security.authentication.AuthenticationProvider#authenticate(org.springframework.security.core.Authentication)}
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
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        final String login = token.getName();
        if (StringUtils.stringSet(login))
        {
            final UserConfiguration user = userManager.getUserConfig(login);
            if (ldapManager.canAutoAdd() && user == null)
            {
                // LDAP servers tend to be case-insensitive for logins, so we
                // do so too (lest we add multiple users with only case
                // differences - CIB-1950).
                User insensitiveUser = findUserCaseInsensitive(login);
                if (insensitiveUser == null)
                {
                    LOG.debug("User '" + login + "' does not exist, asking LDAP manager");
                    token = new UsernamePasswordAuthenticationToken(login, token.getCredentials());

                    final String password = (String) token.getCredentials();
                    SecurityUtils.runAsSystem(new Runnable()
                    {
                        public void run()
                        {
                            tryAutoAdd(login, password);
                        }
                    });
                }
                else
                {
                    // Update the authentication token to match the existing
                    // user's login for case.
                    LOG.debug("Case insensitive match for login '" + login + "': existing login '" + insensitiveUser.getLogin() + "'");
                    token = new UsernamePasswordAuthenticationToken(insensitiveUser.getLogin(), token.getCredentials());
                }
            }

            if (user != null && user.isAuthenticatedViaLdap() && isWebLogin(token))
            {
                SecurityUtils.runAsSystem(new Runnable()
                {
                    public void run()
                    {
                        setRandomPassword(user);
                    }
                });
            }
        }

        return super.authenticate(token);
    }

    private boolean isWebLogin(UsernamePasswordAuthenticationToken token)
    {
        Object details = token.getDetails();
        return details != null && details instanceof WebAuthenticationDetails;
    }

    private User findUserCaseInsensitive(final String login)
    {
        return find(userManager.getAllUsers(), new Predicate<User>()
        {
            public boolean apply(User user)
            {
                UserConfiguration userConfig = user.getConfig();
                return userConfig.isAuthenticatedViaLdap() && userConfig.getLogin().equalsIgnoreCase(login);
            }
        }, null);
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
        userManager.setPassword(user, RandomUtils.randomToken(10));
    }

    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
    {
        Principle user = (Principle) userDetails;
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
