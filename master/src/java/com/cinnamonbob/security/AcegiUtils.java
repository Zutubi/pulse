package com.cinnamonbob.security;

import com.cinnamonbob.model.User;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.context.SecurityContextHolder;

/**
 * <class-comment/>
 */
public class AcegiUtils
{
    public static void loginAs(User targetUser)
    {
        UsernamePasswordAuthenticationToken targetUserRequest =
                new UsernamePasswordAuthenticationToken(targetUser, targetUser.getPassword(), targetUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(targetUserRequest);
    }
}
