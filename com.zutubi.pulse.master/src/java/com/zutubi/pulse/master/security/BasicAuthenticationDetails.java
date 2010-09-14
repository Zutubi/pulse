package com.zutubi.pulse.master.security;

import org.springframework.security.ui.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * An extended details class that exists so that we can distinguish authentation
 * via HTTP basic via the type of the details object.  That is, if the type is
 * this class, we know the user was authentication via HTTP basic auth.
 */
public class BasicAuthenticationDetails extends WebAuthenticationDetails
{
    public BasicAuthenticationDetails(HttpServletRequest request)
    {
        super(request);
    }
}
