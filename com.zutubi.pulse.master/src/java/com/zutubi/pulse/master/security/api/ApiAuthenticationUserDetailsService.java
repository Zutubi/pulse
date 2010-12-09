package com.zutubi.pulse.master.security.api;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.UserManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Implementation of the {@link org.springframework.security.core.userdetails.AuthenticationUserDetailsService}
 * interface that loads a principle via the {@link com.zutubi.pulse.master.model.UserManager}.
 *
 * This user details source is used by the {@link org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider}
 * that is used to support api token access to the web ui.
 *
 * @see org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider
 * @see com.zutubi.pulse.master.security.api.ApiPreAuthenticatedProcessingFilter
 * @see com.zutubi.pulse.servercore.api.APIAuthenticationToken
 * @see com.zutubi.pulse.master.api.DefaultTokenManager
 */
public class ApiAuthenticationUserDetailsService implements AuthenticationUserDetailsService
{
    private UserManager userManager;

    public UserDetails loadUserDetails(Authentication token) throws UsernameNotFoundException
    {
        return userManager.getPrinciple((User)token.getPrincipal());
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
