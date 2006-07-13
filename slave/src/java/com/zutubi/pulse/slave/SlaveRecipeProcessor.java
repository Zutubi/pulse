package com.zutubi.pulse.slave;

import com.zutubi.pulse.ChainBootstrapper;
import com.zutubi.pulse.ServerBootstrapper;
import com.zutubi.pulse.ServerRecipePaths;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.services.MasterService;
import com.zutubi.pulse.services.ServiceTokenManager;
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

    public SlaveRecipeProcessor()
    {
        // TODO on startup, clean out any existing working/output directories left around
    }

    private EventListener registerMasterListener(MasterService service, long id)
    {
        EventListener listener = new ForwardingEventListener(service, serviceTokenManager, id);
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

    public void processRecipe(String master, long slaveId, RecipeRequest request)
    {
        MasterService masterProxy = getMasterProxy(master);
        if(masterProxy != null)
        {
            EventListener listener = registerMasterListener(masterProxy, request.getId());
            ResourceRepository repo = new RemoteResourceRepository(slaveId, masterProxy, serviceTokenManager);
            ServerRecipePaths processorPaths = new ServerRecipePaths(request.getId(), configurationManager.getUserPaths().getData());
            request.setBootstrapper(new ChainBootstrapper(new ServerBootstrapper(), request.getBootstrapper()));

            try
            {
                recipeProcessor.build(request, processorPaths, repo, true);
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
