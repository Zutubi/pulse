package com.zutubi.pulse.master.security;

import javax.servlet.ServletException;

/**
 * Overrides the default Acegi implementation to force eager session
 * creation.  Configuring this by normal means does not work due to a
 * combination of needing this filter around the sitemesh one and the fact
 * that we dynamically deploy the rest of the security filters at run time
 * (which makes them within sitemesh, meaning the security context is cleaned
 * out of the session and sitemesh does not see the logged in user).
 */
public class HttpSessionContextIntegrationFilter extends org.acegisecurity.context.HttpSessionContextIntegrationFilter
{
    public HttpSessionContextIntegrationFilter() throws ServletException
    {
        setForceEagerSessionCreation(true);
    }
}
