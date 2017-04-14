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

import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Allows users to perform actions on their own configuration.
 */
public class UserConfigurationAuthorityProvider implements AuthorityProvider<UserConfiguration>
{
    public Set<String> getAllowedAuthorities(String action, UserConfiguration resource)
    {
        Set<String> result = new HashSet<String>(1);
        // The user can do whatever to themself.
        result.add(resource.getDefaultAuthority());
        return result;
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(UserConfiguration.class, this);
    }
}
