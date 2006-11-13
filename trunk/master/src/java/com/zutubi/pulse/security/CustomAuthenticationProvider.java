package com.zutubi.pulse.security;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.security.ldap.LdapManager;
import com.zutubi.pulse.util.logging.Logger;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.dao.DaoAuthenticationProvider;
import org.acegisecurity.userdetails.UserDetails;

/**
 */
public class CustomAuthenticationProvider extends DaoAuthenticationProvider
{
    private static final Logger LOG = Logger.getLogger(CustomAuthenticationProvider.class);

    private UserManager userManager;
    private LdapManager ldapManager;

    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        // Just check for non-existent user auto-adding.
        if(ldapManager.canAutoAdd())
        {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;

            User user = userManager.getUser(token.getName());
            if(user == null)
            {
                LOG.debug("User '" + token.getName() + "' does not exist, asking LDAP manager");
                tryAutoAdd(token.getName(), (String) token.getCredentials());
            }
        }

        return super.authenticate(authentication);
    }

    private void tryAutoAdd(String username, String password)
    {
        LOG.debug("Testing for user '" + username + "' via LDAP");

        // We can auto-add the user if they can be authenticated via LDAP.
        User user = ldapManager.authenticate(username, password);
        if(user != null)
        {
            LOG.debug("User '" + username + "' found via LDAP, adding.");
            userManager.addUser(user, false, true);
        }
        else
        {
            LOG.debug("User '" + username + "' could not be authenticated via LDAP");
        }
    }

    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
    {
        User user = (User) userDetails;
        if(user.getLdapAuthentication())
        {
            LOG.debug("Authenticating user '" + user.getLogin() + "' via LDAP");
            if(ldapManager.authenticate(authentication.getName(), authentication.getCredentials().toString()) == null)
            {
                LOG.debug("Authentication failed for user '" + user.getLogin() + "' via LDAP");
                throw new BadCredentialsException("Bad credentials");
            }
        }
        else
        {
            LOG.debug("Authenticating user '" + user.getLogin() + "' via stored password");
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
