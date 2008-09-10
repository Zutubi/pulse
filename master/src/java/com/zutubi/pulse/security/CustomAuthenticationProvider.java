package com.zutubi.pulse.security;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.system.ConfigurationEventSystemStartedEvent;
import com.zutubi.pulse.model.AcegiUser;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.security.ldap.LdapManager;
import com.zutubi.pulse.tove.config.user.UserConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationRegistry;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.RandomUtils;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.dao.DaoAuthenticationProvider;
import org.acegisecurity.userdetails.UserDetails;

/**
 */
public class CustomAuthenticationProvider extends DaoAuthenticationProvider implements EventListener
{
    private static final Logger LOG = Logger.getLogger(CustomAuthenticationProvider.class);

    private UserManager userManager;
    private LdapManager ldapManager;
    private ConfigurationProvider configurationProvider;

    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        // Just check for non-existent user auto-adding.
        if(ldapManager.canAutoAdd())
        {
            final UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;

            User user = userManager.getUser(token.getName());
            if(user == null)
            {
                LOG.debug("User '" + token.getName() + "' does not exist, asking LDAP manager");
                AcegiUtils.runAsSystem(new Runnable()
                {
                    public void run()
                    {
                        tryAutoAdd(token.getName(), (String) token.getCredentials());
                    }
                });
            }
        }

        return super.authenticate(authentication);
    }

    private void tryAutoAdd(String username, String password)
    {
        LOG.debug("Testing for user '" + username + "' via LDAP");

        // We can auto-add the user if they can be authenticated via LDAP.
        UserConfiguration user = ldapManager.authenticate(username, password, true);
        if(user != null)
        {
            LOG.debug("User '" + username + "' found via LDAP, adding.");
            String newUserPath = configurationProvider.insert(ConfigurationRegistry.USERS_SCOPE, user);

            // refresh so that we have the persistent user instance.
            user = configurationProvider.get(newUserPath, UserConfiguration.class);
            
            // auto added users receive a random password that allows the remember me processing function.
            // (it fails if no password is set, and we do not record the LDAP password).
            userManager.setPassword(user, RandomUtils.randomString(10));
        }
        else
        {
            LOG.debug("User '" + username + "' could not be authenticated via LDAP");
        }
    }

    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
    {
        AcegiUser user = (AcegiUser) userDetails;
        if(user.getLdapAuthentication())
        {
            LOG.debug("Authenticating user '" + user.getUsername() + "' via LDAP");
            if(ldapManager.authenticate(authentication.getName(), authentication.getCredentials().toString(), false) == null)
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

    public void handleEvent(Event event)
    {
        configurationProvider = ((ConfigurationEventSystemStartedEvent) event).getConfigurationProvider();
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ ConfigurationEventSystemStartedEvent.class };
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

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }
}
