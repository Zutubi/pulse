package com.zutubi.pulse.master.rest;

import com.zutubi.pulse.master.security.CustomRememberMeServices;
import com.zutubi.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for the /auth section of the RESTish API.
 */
@RestController
@RequestMapping("/auth")
public class AuthController
{
    public static final Logger LOG = Logger.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CustomRememberMeServices rememberMeServices;

    @RequestMapping(value = "/session", method = RequestMethod.POST)
    public ResponseEntity<Session> session(@RequestBody Credentials credentials, HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword());
            authRequest.setDetails(new WebAuthenticationDetails(request));
            Authentication authResult = authenticationManager.authenticate(authRequest);

            SecurityContextHolder.getContext().setAuthentication(authResult);
            if (credentials.rememberMe)
            {
                // Communicate the user requested rememberMe to our CustomRememberMeServices.
                request.setAttribute(rememberMeServices.getParameter(), true);
            }

            rememberMeServices.loginSuccess(request, response, authResult);
            LOG.debug("RESTish login success for '" + credentials.username + "'");

            return new ResponseEntity<>(new Session(request.getSession().getId(), request.getSession().getCreationTime()), HttpStatus.OK);
        }
        catch (AuthenticationException failed)
        {
            LOG.debug(failed);

            SecurityContextHolder.clearContext();
            rememberMeServices.loginFail(request, response);

            throw failed;
        }
    }

    public static class Credentials
    {
        private String username;
        private String password;
        private boolean rememberMe;

        public Credentials()
        {
        }

        public String getUsername()
        {
            return username;
        }

        public void setUsername(String username)
        {
            this.username = username;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }

        public boolean isRememberMe()
        {
            return rememberMe;
        }

        public void setRememberMe(boolean rememberMe)
        {
            this.rememberMe = rememberMe;
        }
    }

    public static class Session
    {
        private String sessionId;
        private long creationTime;

        public Session(String sessionId, long creationTime)
        {
            this.sessionId = sessionId;
            this.creationTime = creationTime;
        }

        public String getSessionId()
        {
            return sessionId;
        }

        public long getCreationTime()
        {
            return creationTime;
        }
    }
}
