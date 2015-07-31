package com.zutubi.pulse.master.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import javax.servlet.http.HttpServletRequest;

/**
 * Our extension of token-based remember me that adds handling of remember me requests via RESTish
 * API calls.
 */
public class CustomRememberMeServices extends TokenBasedRememberMeServices
{
    public CustomRememberMeServices(String key, UserDetailsService userDetailsService)
    {
        super(key, userDetailsService);
    }

    @Override
    protected boolean rememberMeRequested(HttpServletRequest request, String parameter)
    {
        return super.rememberMeRequested(request, parameter) || request.getAttribute(parameter) != null;
    }
}
