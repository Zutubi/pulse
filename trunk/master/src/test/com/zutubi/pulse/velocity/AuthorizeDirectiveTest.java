package com.zutubi.pulse.velocity;

import com.zutubi.pulse.security.AcegiUtils;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.apache.velocity.app.VelocityEngine;

/**
 * <class-comment/>
 */
public class AuthorizeDirectiveTest extends VelocityDirectiveTestCase
{
    private VelocityEngine velocity;

    public AuthorizeDirectiveTest(String testName)
    {
        super(testName);
    }

    public String[] getUserDirectives()
    {
        return new String[]{"com.zutubi.pulse.velocity.AuthorizeDirective"};
    }

    public void testIfAllGranted() throws Exception
    {
        // setup and login the principle...
        AcegiUtils.loginAs(new UserDetailsAdapter("ROLE_A", "ROLE_B"));

        assertEquals("", evaluate("#authorize()Authorized#end"));
        assertEquals("Authorized", evaluate("#authorize(\"ifAllGranted=ROLE_A\")Authorized#end"));
        assertEquals("Authorized", evaluate("#authorize(\"ifAllGranted=ROLE_A,ROLE_B\")Authorized#end"));
        assertEquals("", evaluate("#authorize(\"ifAllGranted=ROLE_A,ROLE_B,ROLE_C\")Authorized#end"));
    }

    public void testIfAnyGranted() throws Exception
    {
        // setup and login the principle...
        AcegiUtils.loginAs(new UserDetailsAdapter("ROLE_A"));

        assertEquals("", evaluate("#authorize()Authorized#end"));
        assertEquals("Authorized", evaluate("#authorize(\"ifAnyGranted=ROLE_A\")Authorized#end"));
        assertEquals("Authorized", evaluate("#authorize(\"ifAnyGranted=ROLE_A,ROLE_B\")Authorized#end"));
        assertEquals("", evaluate("#authorize(\"ifAnyGranted=ROLE_B,ROLE_C\")Authorized#end"));
    }

    public void testIfNotGranted() throws Exception
    {
        // setup and login the principle...
        AcegiUtils.loginAs(new UserDetailsAdapter("ROLE_A"));

        assertEquals("", evaluate("#authorize()Authorized#end"));
        assertEquals("", evaluate("#authorize(\"ifNotGranted=ROLE_A\")Authorized#end"));
        assertEquals("Authorized", evaluate("#authorize(\"ifNotGranted=ROLE_B\")Authorized#end"));
        assertEquals("Authorized", evaluate("#authorize(\"ifNotGranted=ROLE_B,ROLE_C\")Authorized#end"));
        assertEquals("", evaluate("#authorize(\"ifNotGranted=ROLE_A,ROLE_B\")Authorized#end"));
    }

    private class UserDetailsAdapter implements UserDetails
    {
        private GrantedAuthority[] authorities;

        public UserDetailsAdapter(String... roles)
        {
            authorities = new GrantedAuthority[roles.length];
            for (int i = 0; i < roles.length; i++)
            {
                authorities[i] = new GrantedAuthorityAdapter(roles[i]);
            }
        }

        public GrantedAuthority[] getAuthorities()
        {
            return authorities;
        }

        public String getPassword()
        {
            return "test";
        }

        public String getUsername()
        {
            return "test";
        }

        public boolean isAccountNonExpired()
        {
            return true;
        }

        public boolean isAccountNonLocked()
        {
            return true;
        }

        public boolean isCredentialsNonExpired()
        {
            return true;
        }

        public boolean isEnabled()
        {
            return true;
        }
    }

    private class GrantedAuthorityAdapter implements GrantedAuthority
    {
        private String authority;

        public GrantedAuthorityAdapter(String authority)
        {
            this.authority = authority;
        }

        public String getAuthority()
        {
            return authority;
        }
    }
}