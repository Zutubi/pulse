package com.zutubi.pulse.master.security;

import org.springframework.security.authentication.AuthenticationDetailsSource;

import javax.servlet.http.HttpServletRequest;

/**
 * An factory for {@link com.zutubi.pulse.master.security.BasicAuthenticationDetails}, used to
 * configure Spring's HTTP Basic Auth filter to create this specific type of auth details, making
 * it possible for us to detect auth via HTTP basic later on.
 */
public class BasicAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, BasicAuthenticationDetails>
{
    public BasicAuthenticationDetails buildDetails(HttpServletRequest context)
    {
        return new BasicAuthenticationDetails(context);
    }
}
