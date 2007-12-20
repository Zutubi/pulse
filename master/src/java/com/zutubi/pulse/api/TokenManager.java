package com.zutubi.pulse.api;

import com.zutubi.pulse.model.User;

/**
 */
public interface TokenManager
{
    String login(String username, String password) throws AuthenticationException;

    String login(String username, String password, long expiry) throws AuthenticationException;

    boolean logout(String token);

    User verifyAdmin(String token) throws AuthenticationException;

    User verifyUser(String token) throws AuthenticationException;

    User verifyRoleIn(String token, String... allowedAuthorities) throws AuthenticationException;

    User loginUser(String token) throws AuthenticationException;

    void logoutUser();
}
