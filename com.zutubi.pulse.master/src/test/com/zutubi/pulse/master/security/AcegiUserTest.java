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

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.Role;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.group.BuiltinGroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.util.Sort;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;


/**
 */
public class AcegiUserTest extends PulseTestCase
{
    public void testAuthoritiesNone()
    {
        User u = newUser("a", "b");
        assertAuthorities(u);
    }

    public void testAuthoritiesUser()
    {
        User u = newUser("a", "b");
        u.getConfig().addDirectAuthority("auth");
        u.getConfig().addDirectAuthority("two");
        assertAuthorities(u, "auth", "two");
    }

    public void testAuthoritiesGroupAdditional()
    {
        UserGroupConfiguration g = new UserGroupConfiguration("g1");
        g.addServerPermission(ServerPermission.CREATE_PROJECT);
        Principle a = new Principle(newUser("a", "b"), Arrays.asList(g));
        assertAuthorities(a, "group:g1", ServerPermission.CREATE_PROJECT.toString());
    }

    public void testAuthoritiesTransient()
    {
        User u = newUser("a", "b");
        Principle a = new Principle(u, Collections.<UserGroupConfiguration>emptyList());
        a.addGroup(new BuiltinGroupConfiguration("test", "tran"));
        assertAuthorities(a, "tran");
    }

    public void testAuthoritiesMultiple()
    {
        User u = newUser("a", "b");
        UserGroupConfiguration g = new UserGroupConfiguration("g1");
        g.setHandle(1);
        g.addServerPermission(ServerPermission.CREATE_PROJECT);
        Principle a = new Principle(u, Arrays.asList(g));
        a.addGroup(new BuiltinGroupConfiguration("test", "ta"));
        assertAuthorities(a, "group:g1", ServerPermission.CREATE_PROJECT.toString(), "ta");
    }

    private User newUser(String login, String name)
    {
        User user = new User();
        UserConfiguration config = new UserConfiguration(login, name);
        user.setConfig(config);
        return user;
    }

    private void assertAuthorities(User u, String... expected)
    {
        Principle principle = new Principle(u, Collections.<UserGroupConfiguration>emptyList());
        assertAuthorities(principle, expected);
    }

    private void assertAuthorities(Principle principle, String... expected)
    {
        // Add the authorities that all users have to the expectation
        List<String> actualExpected = new LinkedList<String>();
        actualExpected.addAll(Arrays.asList(expected));
        actualExpected.add("user:" + principle.getUsername());
        actualExpected.add(Role.USER);
        final Sort.StringComparator sc = new Sort.StringComparator();
        Collections.sort(actualExpected, sc);

        List<GrantedAuthority> authorities = new LinkedList<GrantedAuthority>(principle.getAuthorities());
        Collections.sort(authorities, new Comparator<GrantedAuthority>()
        {
            public int compare(GrantedAuthority o1, GrantedAuthority o2)
            {
                return sc.compare(o1.getAuthority(), o2.getAuthority());
            }
        });

        assertEquals(actualExpected.size(), authorities.size());
        for(int i = 0; i < authorities.size(); i++)
        {
            assertEquals(actualExpected.get(i), authorities.get(i).getAuthority());
        }
    }
}
