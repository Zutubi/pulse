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

package com.zutubi.tove.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Basic implementation of the Actor interface.
 */
public class DefaultActor implements Actor
{
    private String username;
    private Set<String> grantedAuthorities = new HashSet<String>();

    public DefaultActor(String username, String... authorities)
    {
        this.username = username;
        grantedAuthorities.addAll(Arrays.asList(authorities));
    }

    public String getUsername()
    {
        return username;
    }

    public Set<String> getGrantedAuthorities()
    {
        return grantedAuthorities;
    }

    public boolean isAnonymous()
    {
        return false;
    }

    public void addGrantedAuthority(String authority)
    {
        grantedAuthorities.add(authority);
    }
}
