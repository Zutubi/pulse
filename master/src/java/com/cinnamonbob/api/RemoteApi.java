package com.cinnamonbob.api;

import com.cinnamonbob.ShutdownManager;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.model.User;
import com.cinnamonbob.bootstrap.ComponentContext;

/**
 * Implements a simple API for remote monitoring and control.
 */
public class RemoteApi
{
    private TokenManager tokenManager;
    private ShutdownManager shutdownManager;
    private UserManager userManager;

    public RemoteApi()
    {
        // can remove this call when we sort out autowiring from the XmlRpcServlet.
        ComponentContext.autowire(this);
    }

    public String login(String username, String password) throws AuthenticationException
    {
        return tokenManager.login(username, password);
    }

    public boolean logout(String token)
    {
        return tokenManager.logout(token);
    }

    public boolean shutdown(String token, boolean force) throws AuthenticationException
    {
        // Sigh ... this is tricky, because if we shutdown here Jetty dies
        // before this request is complete and the client gets an error :-|.
        tokenManager.verifyAdmin(token);

        ShutdownRunner runner = new ShutdownRunner(force);
        new Thread(runner).start();
        return true;
    }

    public void setPassword(String token, String username, String password) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);

        User user = userManager.getUser(username);
        if (user == null)
        {
            throw new IllegalArgumentException("unknown username '"+username+"'");
        }
        
        user.setPassword(password);
        userManager.save(user);
    }

    /**
     * Required resource.
     *
     * @param tokenManager
     */
    public void setTokenManager(TokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }

    /**
     * Required resource.
     *
     * @param shutdownManager
     */
    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }

    /**
     * Required resource.
     *
     * @param userManager
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    private class ShutdownRunner implements Runnable
    {
        private boolean force;

        public ShutdownRunner(boolean force)
        {
            this.force = force;
        }

        public void run()
        {
            // Oh my, is this ever dodgy...
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                // Empty
            }
            shutdownManager.shutdown(force);
        }
    }
}
