package com.zutubi.pulse.model;

import com.zutubi.pulse.test.PulseTestCase;

/**
 */
public class AcegiUserTest extends PulseTestCase
{
    public void testAuthoritiesNone()
    {
        User u = new User("a", "b");
        assertAuthorities(u);
    }

    public void testAuthoritiesUser()
    {
        User u = new User("a", "b");
        u.add("auth");
        u.add("two");
        assertAuthorities(u, "auth", "two");
    }

    public void testAuthoritiesGroup()
    {
        User u = new User("a", "b");
        Group g = new Group("g1");
        g.setId(10);
        u.addGroup(g);
        assertAuthorities(u, "GROUP_10");
    }

    public void testAuthoritiesGroupAdditional()
    {
        User u = new User("a", "b");
        Group g = new Group("g1");
        g.setId(10);
        g.addAdditionalAuthority("foo");
        u.addGroup(g);
        assertAuthorities(u, "GROUP_10", "foo");
    }

    public void testAuthoritiesTransient()
    {
        User u = new User("a", "b");
        AcegiUser a = new AcegiUser(u);
        a.addTransientAuthority("tran");
        assertAuthorities(a, "tran");
    }

    public void testAuthoritiesMultiple()
    {
        User u = new User("a", "b");
        u.add("ua");
        Group g = new Group("g1");
        g.setId(1);
        g.addAdditionalAuthority("ga");
        u.addGroup(g);
        AcegiUser a = new AcegiUser(u);
        a.addTransientAuthority("ta");
        assertAuthorities(a, "ua", "GROUP_1", "ga", "ta");
    }

    private void assertAuthorities(User u, String... expected)
    {
        AcegiUser acegiUser = new AcegiUser(u);
        assertAuthorities(acegiUser, expected);
    }

    private void assertAuthorities(AcegiUser acegiUser, String... expected)
    {
        org.acegisecurity.GrantedAuthority[] authorities = acegiUser.getAuthorities();
        assertEquals(expected.length, authorities.length);
        for(int i = 0; i < authorities.length; i++)
        {
            assertEquals(expected[i], authorities[i].getAuthority());
        }
    }
}
