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
