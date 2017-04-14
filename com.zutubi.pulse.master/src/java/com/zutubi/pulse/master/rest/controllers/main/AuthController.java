/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.rest.controllers.main;

import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.security.CustomRememberMeServices;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.user.SignupUserConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.ui.ToveUiUtils;
import com.zutubi.tove.ui.model.CompositeModel;
import com.zutubi.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
    @Autowired
    private TypeRegistry typeRegistry;
    @Autowired
    private ConfigurationTemplateManager configurationTemplateManager;
    @Autowired
    private UserManager userManager;

    @RequestMapping(value = "/session", method = RequestMethod.GET)
    public ResponseEntity<Session> getSession(HttpServletRequest request)
    {
        return getSessionResponse(request);
    }

    @RequestMapping(value = "/session", method = RequestMethod.POST)
    public ResponseEntity<Session> postSession(@RequestBody Credentials credentials, HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            loginWithCredentials(credentials, request, response);
            return getSessionResponse(request);
        }
        catch (AuthenticationException failed)
        {
            LOG.debug(failed);

            SecurityContextHolder.clearContext();
            rememberMeServices.loginFail(request, response);

            throw failed;
        }
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ResponseEntity<Session> postSession(@RequestBody CompositeModel body, HttpServletRequest request, HttpServletResponse response) throws TypeException
    {
        if (!configurationTemplateManager.getInstance(GlobalConfiguration.SCOPE_NAME, GlobalConfiguration.class).isAnonymousSignupEnabled())
        {
            throw new AccessDeniedException("Anonymous signup is not enabled");
        }

        CompositeType type = typeRegistry.getType(SignupUserConfiguration.class);
        Record record = ToveUiUtils.convertProperties(type, null, body.getProperties());

        SignupUserConfiguration instance = configurationTemplateManager.validate(null, null, record, true, true);
        if (!instance.isValid())
        {
            throw new ValidationException(instance);
        }

        UserConfiguration user = new UserConfiguration(instance.getLogin(), instance.getName());
        user = userManager.insert(user);
        setPassword(user, instance.getPassword());

        loginWithCredentials(new Credentials(instance.getLogin(), instance.getPassword()), request, response);

        return getSessionResponse(request);
    }

    private void setPassword(final UserConfiguration user, final String password)
    {
        SecurityUtils.runAsSystem(new Runnable()
        {
            public void run()
            {
                userManager.setPassword(user, password);
            }
        });
    }

    private void loginWithCredentials(@RequestBody Credentials credentials, HttpServletRequest request, HttpServletResponse response)
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
    }

    private ResponseEntity<Session> getSessionResponse(HttpServletRequest request)
    {
        return new ResponseEntity<>(new Session(request.getSession().getId(), request.getSession().getCreationTime(), SecurityUtils.getLoggedInUsername()), HttpStatus.OK);
    }

    public static class Credentials
    {
        private String username;
        private String password;
        private boolean rememberMe;

        public Credentials()
        {
        }

        public Credentials(String username, String password)
        {
            this.username = username;
            this.password = password;
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
        private String username;

        public Session(String sessionId, long creationTime, String username)
        {
            this.sessionId = sessionId;
            this.creationTime = creationTime;
            this.username = username;
        }

        public String getSessionId()
        {
            return sessionId;
        }

        public long getCreationTime()
        {
            return creationTime;
        }

        public String getUsername()
        {
            return username;
        }
    }
}
