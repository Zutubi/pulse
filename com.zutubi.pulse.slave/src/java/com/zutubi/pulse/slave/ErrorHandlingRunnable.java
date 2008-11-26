package com.zutubi.pulse.slave;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.servercore.services.MasterService;
import com.zutubi.util.logging.Logger;

/**
 * A {@link Runnable} implementation that wraps another runnable and catches
 * any build or unexpected exceptions that it throws.  The exceptions are
 * reported back to the master as recipe errors.  This prevents an error
 * killing the build on a slave without the master realising it.
 */
public class ErrorHandlingRunnable implements Runnable
{
    private static final Logger LOG = Logger.getLogger(ErrorHandlingRunnable.class);

    private MasterService master;
    private String serviceToken;
    private long recipeId;
    private Runnable delegate;

    public ErrorHandlingRunnable(MasterService master, String serviceToken, long recipeId, Runnable delegate)
    {
        this.master = master;
        this.serviceToken = serviceToken;
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
            sendError(e.getMessage());
        }
        catch (Exception e)
        {
            LOG.severe(e);
            sendError("Unexpected error: " + e.getMessage());
        }
    }

    private void sendError(String error)
    {
        RecipeErrorEvent event = new RecipeErrorEvent(null, recipeId, error);

        try
        {
            master.handleEvent(serviceToken, event);
        }
        catch (RuntimeException e)
        {
            LOG.warning("Unable to send error to master '" + master + "': " + e.getMessage(), e);
        }
    }

}
