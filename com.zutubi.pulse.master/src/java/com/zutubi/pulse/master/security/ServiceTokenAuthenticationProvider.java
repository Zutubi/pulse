package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

/**
 * An authentication provider implementation that uses the service token (a shared token
 * between the pulse master and agents) to provide an authentication.
 */
public class ServiceTokenAuthenticationProvider implements AuthenticationProvider
{
    private ServiceTokenManager serviceTokenManager;
    private static final String USERNAME = "pulse";

    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) authentication;
        String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
        if (isPulse(username))
        {
            return validateToken(auth);
        }
        // pass authentication on to another provider
        return null;
    }

    private Authentication validateToken(Authentication auth)
    {
        String token = serviceTokenManager.getToken();
        if (token == null)
        {
            // too early, no tokens have been generated (made available).
            return null;
        }
        if (token.subSequence(0, 12).equals(auth.getCredentials()))
        {
            return createAuthentication();
        }
        return null;
    }

    private Authentication createAuthentication()
    {
        // Provide administer authentication since it will be Pulse making
        // the request.

        // IMPLEMENTATION NOTE:  It would be better to use the 'AcegiUtils.systemUser' but it has a
        // null name which causes problems further on.  Should we give the system user a name?
        
        UserConfiguration config = new UserConfiguration();
        config.setName(USERNAME);
        config.setLogin(USERNAME);
        config.addDirectAuthority(ServerPermission.ADMINISTER.toString());
        AcegiUser agentUser = new AcegiUser(config, null);

        return new UsernamePasswordAuthenticationToken(agentUser, null, agentUser.getAuthorities());
    }

    private boolean isPulse(String name)
    {
        return name != null && name.compareTo(USERNAME) == 0;
    }

    public boolean supports(Class authentication)
    {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }
}
