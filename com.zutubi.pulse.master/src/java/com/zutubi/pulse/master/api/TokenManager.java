package com.zutubi.pulse.master.api;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.servercore.api.AuthenticationException;

/**
 */
public interface TokenManager
{
    String login(String username, String password) throws AuthenticationException;

    String login(String username, String password, long expiry) throws AuthenticationException;

    boolean logout(String token);

    void verifyAdmin(String token) throws AuthenticationException;

    void verifyUser(String token) throws AuthenticationException;

    void verifyRoleIn(String token, String... allowedAuthorities) throws AuthenticationException;

    void loginUser(String token) throws AuthenticationException;

    User loginAndReturnUser(String token) throws AuthenticationException;

    void logoutUser();
}
