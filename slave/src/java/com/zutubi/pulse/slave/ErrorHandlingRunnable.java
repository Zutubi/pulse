package com.zutubi.pulse.slave;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.services.MasterService;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.util.logging.Logger;

import java.net.MalformedURLException;

/**
 */
public class ErrorHandlingRunnable implements Runnable
{
    private static final Logger LOG = Logger.getLogger(ErrorHandlingRunnable.class);

    private String master;
    private ServiceTokenManager serviceTokenManager;
    private long recipeId;
    private Runnable delegate;
    private MasterProxyFactory masterProxyFactory;

    public ErrorHandlingRunnable(String master, ServiceTokenManager serviceTokenManager, long recipeId, Runnable delegate)
    {
        this.master = master;
        this.serviceTokenManager = serviceTokenManager;
        this.recipeId = recipeId;
        this.delegate = delegate;
    }

    public void run()
    {
        try
        {
            delegate.run();
        }
        catch (BuildException e)
        {
            LOG.warning(e);
            sendError(e);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            sendError(new BuildException("Unexpected error: " + e.getMessage(), e));
        }
    }

    private void sendError(BuildException error)
    {
        RecipeErrorEvent event = new RecipeErrorEvent(null, recipeId, error.getMessage());

        try
        {
            MasterService service = masterProxyFactory.createProxy(master);
            service.handleEvent(serviceTokenManager.getToken(), event);
        }
        catch (MalformedURLException e)
        {
            LOG.warning(e);
        }
        catch (RuntimeException e)
        {
            LOG.warning("Unable to send error to master '" + master + "': " + e.getMessage(), e);
        }
    }

    public void setMasterProxyFactory(MasterProxyFactory masterProxyFactory)
    {
        this.masterProxyFactory = masterProxyFactory;
    }
}
