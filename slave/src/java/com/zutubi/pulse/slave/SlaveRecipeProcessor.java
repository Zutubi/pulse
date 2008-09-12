package com.zutubi.pulse.slave;

import com.zutubi.pulse.*;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.repository.SlaveFileRepository;
import com.zutubi.pulse.services.MasterService;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.util.FileSystem;
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
    private ServiceTokenManager serviceTokenManager;
    private RecipeCleanup recipeCleanup;

    public SlaveRecipeProcessor()
    {
        recipeCleanup = new RecipeCleanup(new FileSystem());
    }

    private EventListener registerMasterListener(String master, MasterService service, long id)
    {
        EventListener listener = new ForwardingEventListener(master, service, serviceTokenManager, id);
        eventManager.register(listener);
        return listener;
    }

    private MasterService getMasterProxy(String master)
    {
        try
        {
            return masterProxyFactory.createProxy(master);
        }
        catch (MalformedURLException e)
        {
            // There is no way we can let the master know: the best thing we
            // can do is log the problem.
            LOG.severe("Could not create connection to master '" + master + "'", e);
        }

        return null;
    }

    public void processRecipe(String master, long slaveId, RecipeRequest request, BuildContext context)
    {
        MasterService masterProxy = getMasterProxy(master);
        if(masterProxy != null)
        {
            EventListener listener = registerMasterListener(master, masterProxy, request.getId());
            ResourceRepository repo = new RemoteResourceRepository(slaveId, masterProxy, serviceTokenManager);
            ServerRecipePaths processorPaths = new ServerRecipePaths(request.getProject(), request.getSpec(), request.getId(), configurationManager.getUserPaths().getData(), request.isIncremental());

            context.setFileRepository(new SlaveFileRepository(processorPaths.getRecipeRoot(), master, serviceTokenManager));
            Bootstrapper requestBootstrapper = request.getBootstrapper();
            request.setBootstrapper(new ChainBootstrapper(new ServerBootstrapper(), requestBootstrapper));

            try
            {
                recipeCleanup.cleanup(eventManager, processorPaths.getRecipesRoot(), request.getId());
                recipeProcessor.build(request, processorPaths, repo, true, context);
            }
            catch (BuildException e)
            {
                LOG.warning("A problem occured while processing a recipe build request. Reason: " + e.getMessage(), e);
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

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    public long getBuildingRecipe()
    {
        return recipeProcessor.getBuildingRecipe();
    }
}
