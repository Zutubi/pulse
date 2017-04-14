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

package com.zutubi.pulse.master.bootstrap;

import com.zutubi.pulse.master.api.TokenManager;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.servercore.api.AdminTokenManager;
import com.zutubi.pulse.servercore.api.AuthenticationException;

/**
 */
public class StartupTokenManager implements TokenManager
{
    private AdminTokenManager adminTokenManager;

    public String login(String username, String password) throws AuthenticationException
    {
        throw new AuthenticationException("System startup in progress");
    }

    public String login(String username, String password, long expiry) throws AuthenticationException
    {
        throw new AuthenticationException("System startup in progress");
    }

    public boolean logout(String token)
    {
        return false;
    }

    public void verifyAdmin(String token) throws AuthenticationException
    {
        if (!adminTokenManager.checkAdminToken(token))
        {
            throw new AuthenticationException("Invalid token");
        }
    }

    public void verifyUser(String token) throws AuthenticationException
    {
        throw new AuthenticationException("System startup in progress");
    }

    public void verifyRoleIn(String token, String... allowedAuthorities) throws AuthenticationException
    {
        throw new AuthenticationException("System startup in progress");
    }

    public void loginUser(String token) throws AuthenticationException
    {
        throw new AuthenticationException("System startup in progress");
    }

    public User loginAndReturnUser(String token) throws AuthenticationException
    {
        throw new AuthenticationException("System startup in progress");
    }

    public void logoutUser()
    {
    }

    public void setAdminTokenManager(AdminTokenManager adminTokenManager)
    {
        this.adminTokenManager = adminTokenManager;
    }
}
