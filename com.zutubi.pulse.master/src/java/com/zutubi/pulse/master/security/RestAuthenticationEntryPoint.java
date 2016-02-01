package com.zutubi.pulse.master.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A custom entry point made for clients of the REST API that uses our own authentication scheme
 * modelled on HTTP Basic but with a custom name to avoid browsers handling it themselves.  We
 * don't want the browser prompting for or storing these credentials.  So we name our scheme
 * 'PulseAPI' so the browser lets our JS clients take care of it without interfering.
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint
{
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException
    {
        response.addHeader("WWW-Authenticate", "PulseAPI realm=\"Pulse\"");
        response.getOutputStream().print("{type: 'NotAuthorized', message: 'Authentication required'}");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
