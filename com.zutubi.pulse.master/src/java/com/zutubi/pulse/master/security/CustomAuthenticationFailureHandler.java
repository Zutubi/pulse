package com.zutubi.pulse.master.security;

import com.zutubi.util.WebUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A customised auth failure handler that adds the attempted username as a parameter to our
 * redirect (so the login form can be populated with it).
 */
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler
{
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException
    {
        super.setDefaultFailureUrl("/login!input.action?" + WebUtils.buildQueryString("error", "true", "username", request.getParameter("username")));
        super.onAuthenticationFailure(request, response, exception);
    }
}
