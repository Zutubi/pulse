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

package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.model.User;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;

import java.util.Set;

/**
 * Provides authorities for users by delegating to
 * {@link com.zutubi.pulse.master.security.UserConfigurationAuthorityProvider}.
 */
public class UserAuthorityProvider implements AuthorityProvider<User>
{
    private UserConfigurationAuthorityProvider userConfigurationAuthorityProvider;

    public Set<String> getAllowedAuthorities(String action, User resource)
    {
        return userConfigurationAuthorityProvider.getAllowedAuthorities(action, resource.getConfig());
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(User.class, this);
    }

    public void setUserConfigurationAuthorityProvider(UserConfigurationAuthorityProvider userConfigurationAuthorityProvider)
    {
        this.userConfigurationAuthorityProvider = userConfigurationAuthorityProvider;
    }
}
