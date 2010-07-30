package com.zutubi.pulse.master.security;

import org.springframework.security.AuthenticationException;
import org.springframework.security.ui.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An AuthenticationEntryPoint that always returns an HTTP error.  Used for
 * Ajax requests where we don't want to redirect to the login page.
 */
public class AjaxAuthenticationEntryPoint implements AuthenticationEntryPoint
{
    public void commence(ServletRequest request, ServletResponse response, AuthenticationException authException) throws IOException, ServletException
    {
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
