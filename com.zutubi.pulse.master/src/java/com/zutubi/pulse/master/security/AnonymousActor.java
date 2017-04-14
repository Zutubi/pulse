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

import com.zutubi.tove.security.Actor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Set;

/**
 * A simple actor that has no username but may be granted authorities.
 */
public class AnonymousActor implements Actor
{
    private Set<String> grantedAuthorities = new HashSet<String>();

    public AnonymousActor(Authentication authentication)
    {
        for(GrantedAuthority a: authentication.getAuthorities())
        {
            grantedAuthorities.add(a.getAuthority());
        }
    }

    public String getUsername()
    {
        return null;
    }

    public Set<String> getGrantedAuthorities()
    {
        return grantedAuthorities;
    }

    public boolean isAnonymous()
    {
        return true;
    }
}
