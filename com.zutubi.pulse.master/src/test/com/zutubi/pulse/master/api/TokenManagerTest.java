package com.zutubi.pulse.master.api;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.GrantedAuthority;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.security.AcegiUser;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.servercore.api.AuthenticationException;
import com.zutubi.util.Constants;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.UserDetails;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class TokenManagerTest extends PulseTestCase
{
    private DefaultTokenManager tokenManager;
    private UserManager userManager;

    protected void setUp() throws Exception
    {
        super.setUp();
        User jason = newUser("jason", "Jason Sankey", "password", GrantedAuthority.USER, ServerPermission.ADMINISTER.toString());
        jason.setId(1);
        User dan = newUser("dan", "Daniel Ostermeier", "insecure", GrantedAuthority.USER);
        dan.setId(2);
        User anon = newUser("anon", "A. Nonymous", "none");
        anon.setId(3);

        userManager = mock(UserManager.class);
        stub(userManager.getUser("jason")).toReturn(jason);
        stub(userManager.getUser("dan")).toReturn(dan);
        stub(userManager.getUser("anon")).toReturn(anon);
        stub(userManager.getUser("nosuchuser")).toReturn(null);
        stub(userManager.getPrinciple(jason)).toReturn(new AcegiUser(jason, null));
        stub(userManager.getPrinciple(dan)).toReturn(new AcegiUser(dan, null));
        stub(userManager.getPrinciple(anon)).toReturn(new AcegiUser(anon, null));

        tokenManager = new DefaultTokenManager();
        tokenManager.setUserManager(userManager);
        tokenManager.setAuthenticationManager(new AuthenticationManager()
        {
            public Authentication authenticate(Authentication authentication) throws org.acegisecurity.AuthenticationException
            {
                UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
                User u = userManager.getUser(token.getName());
                if(u == null)
                {
                    throw new BadCredentialsException("Invalid username");
                }

                if(!u.getConfig().getPassword().equals(token.getCredentials()))
                {
                    throw new BadCredentialsException("Invalid password");
                }

                UserDetails details = userManager.getPrinciple(u);
                return new UsernamePasswordAuthenticationToken(token.getPrincipal(), token.getCredentials(), details.getAuthorities());
            }
        });
    }

    private User newUser(String login, String name, String password, String... authorities)
    {
        User user = new User();
        UserConfiguration config = new UserConfiguration(login, name);
        config.setPassword(password);
        for (String authority: authorities)
        {
            config.addDirectAuthority(authority);
        }
        user.setConfig(config);
        return user;
    }

    public void testLoginUnknownUser() throws Exception
    {
        try
        {
            tokenManager.login("nosuchuser", "");
        }
        catch (AuthenticationException e)
        {
            assertEquals("Invalid username", e.getMessage());
        }
    }

    public void testLoginWrongPassword() throws Exception
    {
        try
        {
            tokenManager.login("jason", "wrong");
        }
        catch (AuthenticationException e)
        {
            assertEquals("Invalid password", e.getMessage());
        }
    }

    public void testLogin() throws Exception
    {
        tokenManager.login("jason", "password");
    }

    public void testLoginLogout() throws Exception
    {
        String token = tokenManager.login("jason", "password");
        assertTrue(tokenManager.logout(token));
        assertFalse(tokenManager.logout(token));
    }

    public void testLogoutInvalid() throws Exception
    {
        assertFalse(tokenManager.logout("bogustoken"));
    }

    public void testUserAccess() throws Exception
    {
        String token = tokenManager.login("jason", "password");
        tokenManager.verifyUser(token);

        token = tokenManager.login("dan", "insecure");
        tokenManager.verifyUser(token);

        token = tokenManager.login("anon", "none");
        try
        {
            tokenManager.verifyUser(token);
        }
        catch (AuthenticationException e)
        {
            assertEquals("Access denied", e.getMessage());
        }
    }

    public void testAdminAccess() throws Exception
    {
        String token = tokenManager.login("jason", "password");
        tokenManager.verifyAdmin(token);

        token = tokenManager.login("dan", "insecure");
        try
        {
            tokenManager.verifyAdmin(token);
        }
        catch (AuthenticationException e)
        {
            assertEquals("Access denied", e.getMessage());
        }

        token = tokenManager.login("anon", "none");
        try
        {
            tokenManager.verifyAdmin(token);
        }
        catch (AuthenticationException e)
        {
            assertEquals("Access denied", e.getMessage());
        }
    }

    public void testUserOrAdminAccess() throws Exception
    {
        String token = tokenManager.login("jason", "password");
        tokenManager.verifyRoleIn(token, GrantedAuthority.USER, ServerPermission.ADMINISTER.toString());

        token = tokenManager.login("dan", "insecure");
        tokenManager.verifyRoleIn(token, GrantedAuthority.USER, ServerPermission.ADMINISTER.toString());

        token = tokenManager.login("anon", "none");
        try
        {
            tokenManager.verifyRoleIn(token, GrantedAuthority.USER, ServerPermission.ADMINISTER.toString());
        }
        catch (AuthenticationException e)
        {
            assertEquals("Access denied", e.getMessage());
        }
    }

    public void testExpiry() throws Exception
    {
        // create a token that expired a minute ago.
        String token = tokenManager.login("jason", "password", Constants.MINUTE * -1);

        assertFalse(tokenManager.logout(token));
    }

    public void testRemoveUser() throws Exception
    {
        String token = tokenManager.login("jason", "password");
        stub(userManager.getUser("jason")).toReturn(null);
        assertFalse(tokenManager.logout(token));
    }

    public void testDetectsStaleTokens() throws Exception
    {
        String firstToken = tokenManager.login("jason", "password", Constants.MINUTE * -1);
        Thread.sleep(10);

        for (int i = 0; i < 1000; i++)
        {
            String token = tokenManager.login("jason", "password");
            assertTrue(tokenManager.logout(token));
        }

        assertFalse(tokenManager.logout(firstToken));
    }
}
