package com.zutubi.pulse.master.security;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.GrantedAuthority;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.group.BuiltinGroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.util.Sort;

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
        AcegiUser a = new AcegiUser(newUser("a", "b"), Arrays.asList(g));
        assertAuthorities(a, "group:g1", ServerPermission.CREATE_PROJECT.toString());
    }

    public void testAuthoritiesTransient()
    {
        User u = newUser("a", "b");
        AcegiUser a = new AcegiUser(u, Collections.<UserGroupConfiguration>emptyList());
        a.addGroup(new BuiltinGroupConfiguration("test", "tran"));
        assertAuthorities(a, "tran");
    }

    public void testAuthoritiesMultiple()
    {
        User u = newUser("a", "b");
        UserGroupConfiguration g = new UserGroupConfiguration("g1");
        g.setHandle(1);
        g.addServerPermission(ServerPermission.CREATE_PROJECT);
        AcegiUser a = new AcegiUser(u, Arrays.asList(g));
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
        AcegiUser acegiUser = new AcegiUser(u, Collections.<UserGroupConfiguration>emptyList());
        assertAuthorities(acegiUser, expected);
    }

    private void assertAuthorities(AcegiUser acegiUser, String... expected)
    {
        // Add the authorities that all users have to the expectation
        List<String> actualExpected = new LinkedList<String>();
        actualExpected.addAll(Arrays.asList(expected));
        actualExpected.add("user:" + acegiUser.getUsername());
        actualExpected.add(GrantedAuthority.USER);
        final Sort.StringComparator sc = new Sort.StringComparator();
        Collections.sort(actualExpected, sc);

        org.acegisecurity.GrantedAuthority[] authorities = acegiUser.getAuthorities();
        Arrays.sort(authorities, new Comparator<org.acegisecurity.GrantedAuthority>()
        {
            public int compare(org.acegisecurity.GrantedAuthority o1, org.acegisecurity.GrantedAuthority o2)
            {
                return sc.compare(o1.getAuthority(), o2.getAuthority());
            }
        });

        assertEquals(actualExpected.size(), authorities.length);
        for(int i = 0; i < authorities.length; i++)
        {
            assertEquals(actualExpected.get(i), authorities[i].getAuthority());
        }
    }
}
