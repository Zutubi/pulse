/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.slave;

import com.caucho.hessian.client.HessianRuntimeException;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.services.MasterService;
import com.zutubi.pulse.util.logging.Logger;

import java.net.MalformedURLException;

/**
 */
public class ErrorHandlingRunnable implements Runnable
{
    private static final Logger LOG = Logger.getLogger(ErrorHandlingRunnable.class);

    private String master;
    private long recipeId;
    private Runnable delegate;
    private MasterProxyFactory masterProxyFactory;

    public ErrorHandlingRunnable(String master, long recipeId, Runnable delegate)
    {
        this.master = master;
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
            service.handleEvent(event);
        }
        catch (MalformedURLException e)
        {
            LOG.warning(e);
        }
        catch (HessianRuntimeException e)
        {
            LOG.warning("Unable to send error to master '" + master + "'", e);
        }
    }

    public void setMasterProxyFactory(MasterProxyFactory masterProxyFactory)
    {
        this.masterProxyFactory = masterProxyFactory;
    }
}
