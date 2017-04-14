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

package com.zutubi.pulse.master.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.util.RandomUtils;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

/**
 * Manages tokens stored in HTTP sessions to prevent against XSRF attacks.  All
 * actions with side effects should:
 *
 * <ol>
 *     <li>Be submitted via POST (to avoid the token appearing in browser history etc)</li>
 *     <li>Include the token in the POST parameters</li>
 * </ol>
 *
 * The {@link com.zutubi.pulse.master.webwork.interceptor.SessionTokenInterceptor}
 * can be used to validate the token is submitted as required.
 */
@SuppressWarnings({"unchecked"})
public class SessionTokenManager
{
    public static final String TOKEN_NAME = "pulse-session-token";

    private static final int TOKEN_LENGTH = 64;

    public static synchronized String getToken()
    {
        Map session = ActionContext.getContext().getSession();
        if (session == null)
        {
            return null;
        }
        
        String token = (String) session.get(TOKEN_NAME);
        if (token == null)
        {
            // First time the token has been requested for the session.
            // Generate a new one.
            token = RandomUtils.secureRandomString(TOKEN_LENGTH);
            session.put(TOKEN_NAME, token);
        }

        return token;
    }

    public static void validateSessionToken()
    {
        Map<String, String[]> parameters = ActionContext.getContext().getParameters();
        String[] tokens = parameters.get(TOKEN_NAME);
        if (tokens == null || tokens.length == 0)
        {
            throw new AccessDeniedException("Missing session token");
        }

        String token = tokens[0];
        if (!token.equals(getToken()))
        {
            throw new AccessDeniedException("Invalid session token");
        }
    }
}
