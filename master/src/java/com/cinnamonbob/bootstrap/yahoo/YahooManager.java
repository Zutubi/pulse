package com.zutubi.pulse.bootstrap.yahoo;

import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.core.BobException;
import com.zutubi.pulse.util.logging.Logger;
import com.opensymphony.util.TextUtils;
import ymsg.network.LoginRefusedException;
import ymsg.network.Session;

import java.io.IOException;

/**
 * The yahoo manager handles the yahoo specific resources. 
 *
 */
public class YahooManager
{
    private static final Logger LOG = Logger.getLogger(YahooManager.class);

    private static final String YAHOO_ID = "yahoo.id";
    private static final String YAHOO_PASSWORD = "yahoo.password";

    private boolean bobIsloggedIn;

    private Throwable problem;

    private Session session;

    private ConfigurationManager configManager;

    public void setConfigurationManager(ConfigurationManager configManager)
    {
        this.configManager = configManager;
    }

    public void login() throws BobException
    {
        if (bobIsloggedIn)
        {
            return;
        }

        String yahooId = null;//configManager.lookupProperty(YAHOO_ID);
        String yahooPassword = null;//configManager.lookupProperty(YAHOO_PASSWORD);

        // if id and password are not configured, then do not attempt to log in.
        if (!TextUtils.stringSet(yahooId) || !TextUtils.stringSet(yahooPassword))
        {
            // bob service is not configured.
            return;
        }

        try
        {
            getSession().login(yahooId, yahooPassword);
            bobIsloggedIn = true;
        }
        catch (IOException e)
        {
            LOG.warning("Failed to log into yahoo.", e);
            problem = e;
        }
        catch (LoginRefusedException e)
        {
            LOG.warning("Failed to log into yahoo.", e);
            problem = e;
        }
    }

    public void logout()
    {
        if (!bobIsloggedIn)
        {
            return;
        }
        try
        {
            getSession().logout();
            bobIsloggedIn = false;
        }
        catch (IOException e)
        {
            LOG.warning("Failed to log out of yahoo.", e);
            problem = e;
        }
    }

    public boolean isLoggedIn()
    {
        return bobIsloggedIn;
    }

    private Session getSession()
    {
        if (session == null)
        {
            session = new Session();
            session.addSessionListener(new SessionAdapter());
        }
        return session;
    }
}
