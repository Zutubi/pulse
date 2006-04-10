package com.zutubi.pulse.api;

import com.zutubi.pulse.model.DefaultUserManager;
import com.zutubi.pulse.model.GrantedAuthority;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.persistence.UserDao;
import com.zutubi.pulse.scheduling.persistence.mock.MockUserDao;
import com.zutubi.pulse.test.BobTestCase;
import com.zutubi.pulse.core.util.Constants;

/**
 */
public class TokenManagerTest extends BobTestCase
{
    DefaultUserManager userManager;
    TokenManager tokenManager;

    protected void setUp() throws Exception
    {
        super.setUp();
        UserDao userDao = new MockUserDao();
        userDao.save(new User("jason", "Jason Sankey", "password", GrantedAuthority.USER, GrantedAuthority.ADMINISTRATOR));
        userDao.save(new User("dan", "Daniel Ostermeier", "insecure", GrantedAuthority.USER));
        userDao.save(new User("anon", "A. Nonymous", "none"));

        userManager = new DefaultUserManager();
        userManager.setUserDao(userDao);
        tokenManager = new TokenManager();
        tokenManager.setUserManager(userManager);
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
        tokenManager.verifyRoleIn(token, GrantedAuthority.USER, GrantedAuthority.ADMINISTRATOR);

        token = tokenManager.login("dan", "insecure");
        tokenManager.verifyRoleIn(token, GrantedAuthority.USER, GrantedAuthority.ADMINISTRATOR);

        token = tokenManager.login("anon", "none");
        try
        {
            tokenManager.verifyRoleIn(token, GrantedAuthority.USER, GrantedAuthority.ADMINISTRATOR);
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
        userManager.delete(userManager.getUser("jason"));
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
