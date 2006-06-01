package com.zutubi.pulse.slave;

import com.zutubi.pulse.ChainBootstrapper;
import com.zutubi.pulse.RecipeRequest;
import com.zutubi.pulse.ServerBootstrapper;
import com.zutubi.pulse.ServerRecipePaths;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.services.MasterService;
import com.zutubi.pulse.util.logging.Logger;

import java.net.MalformedURLException;

/**
 */
public class SlaveRecipeProcessor
{
    private static final Logger LOG = Logger.getLogger(SlaveRecipeProcessor.class);

    private RecipeProcessor recipeProcessor;
    private SlaveConfigurationManager configurationManager;
    private EventManager eventManager;
    private MasterProxyFactory masterProxyFactory;

    public SlaveRecipeProcessor()
    {
        // TODO on startup, clean out any existing working/output directories left around
    }

    private EventListener registerMasterListener(String master, long id)
    {
        try
        {
            MasterService service = masterProxyFactory.createProxy(master);
            EventListener listener = new ForwardingEventListener(service, id);
            eventManager.register(listener);
            return listener;
        }
        catch (MalformedURLException e)
        {
            // There is no way we can let the master know: the best thing we
            // can do is log the problem.
            LOG.severe("Could not create connection to master '" + master + "'", e);
        }

        return null;
    }

    public void processRecipe(String master, RecipeRequest request)
    {
        ServerRecipePaths processorPaths = new ServerRecipePaths(request.getId(), configurationManager.getUserPaths().getData());
        Bootstrapper bootstrapper = new ChainBootstrapper(new ServerBootstrapper(), request.getBootstrapper());
        EventListener listener = registerMasterListener(master, request.getId());

        try
        {
            recipeProcessor.build(request.getId(), processorPaths, bootstrapper, request.getPulseFileSource(), request.getRecipeName());
        }
        catch (BuildException e)
        {
            RecipeErrorEvent error = new RecipeErrorEvent(null, request.getId(), e.getMessage());
            eventManager.publish(error);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            RecipeErrorEvent error = new RecipeErrorEvent(null, request.getId(), "Unexpected error: " + e.getMessage());
            eventManager.publish(error);
        }
        finally
        {
            eventManager.unregister(listener);
        }
    }

    public void setConfigurationManager(SlaveConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setRecipeProcessor(RecipeProcessor recipeProcessor)
    {
        this.recipeProcessor = recipeProcessor;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setMasterProxyFactory(MasterProxyFactory masterProxyFactory)
    {
        this.masterProxyFactory = masterProxyFactory;
    }

    public void terminateRecipe(long id)
    {
        try
        {
            recipeProcessor.terminateRecipe(id);
        }
        catch (InterruptedException e)
        {
            LOG.warning("Interrupted while terminating recipe", e);
        }
    }
}
