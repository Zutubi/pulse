package com.cinnamonbob.slave;

import com.cinnamonbob.ChainBootstrapper;
import com.cinnamonbob.RecipeRequest;
import com.cinnamonbob.ServerBootstrapper;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.Bootstrapper;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.RecipeProcessor;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.events.build.RecipeCommencedEvent;
import com.cinnamonbob.events.build.RecipeCompletedEvent;
import com.cinnamonbob.services.MasterService;
import com.cinnamonbob.util.logging.Logger;

import java.io.File;
import java.net.MalformedURLException;

/**
 */
public class SlaveRecipeProcessor
{
    private static final Logger LOG = Logger.getLogger(SlaveRecipeProcessor.class);

    private RecipeProcessor recipeProcessor;
    private ConfigurationManager configurationManager;
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
        SlaveRecipePaths processorPaths = new SlaveRecipePaths(request.getId(), configurationManager);
        File workDir = processorPaths.getWorkDir();
        File outputDir = processorPaths.getOutputDir();

        Bootstrapper bootstrapper = new ChainBootstrapper(new ServerBootstrapper(), request.getBootstrapper());

        EventListener listener = registerMasterListener(master, request.getId());

        RecipeResult result = new RecipeResult(request.getRecipeName());
        result.setId(request.getId());
        result.commence(outputDir);
        eventManager.publish(new RecipeCommencedEvent(this, result));

        try
        {
            recipeProcessor.build(processorPaths, bootstrapper, request.getBobFile(), request.getRecipeName(), result);
        }
        catch (BuildException e)
        {
            result.error(e);
        }
        finally
        {
            result.complete();
            eventManager.publish(new RecipeCompletedEvent(this, result));
            eventManager.unregister(listener);
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
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
}
