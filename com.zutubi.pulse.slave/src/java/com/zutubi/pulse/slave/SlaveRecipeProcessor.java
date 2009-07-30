package com.zutubi.pulse.slave;

import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.patch.PatchFormatFactory;
import com.zutubi.pulse.servercore.ChainBootstrapper;
import com.zutubi.pulse.servercore.RecipeCleanup;
import com.zutubi.pulse.servercore.ServerBootstrapper;
import com.zutubi.pulse.servercore.ServerRecipePaths;
import com.zutubi.pulse.servercore.services.MasterService;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.pulse.servercore.services.SlaveStatus;
import com.zutubi.pulse.slave.repository.SlaveFileRepository;
import com.zutubi.util.FileSystem;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicLong;

/**
 */
public class SlaveRecipeProcessor
{
    private static final Logger LOG = Logger.getLogger(SlaveRecipeProcessor.class);

    private AtomicLong buildingRecipe = new AtomicLong(SlaveStatus.NO_RECIPE);

    private RecipeProcessor recipeProcessor;
    private SlaveConfigurationManager configurationManager;
    private EventManager eventManager;
    private MasterProxyFactory masterProxyFactory;
    private ServiceTokenManager serviceTokenManager;
    private PatchFormatFactory patchFormatFactory;
    private ScmClientFactory scmClientFactory;
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

    public void processRecipe(String master, long handle, RecipeRequest request)
    {
        MasterService masterProxy = getMasterProxy(master);
        if(masterProxy != null)
        {
            buildingRecipe.set(request.getId());
            PulseExecutionContext context = request.getContext();
            EventListener listener = registerMasterListener(master, masterProxy, request.getId());
            ResourceRepository repo = new RemoteResourceRepository(handle, masterProxy, serviceTokenManager);
            ServerRecipePaths processorPaths = new ServerRecipePaths(request.getProject(), request.getId(), configurationManager.getUserPaths().getData(), context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_INCREMENTAL_BUILD, false));

            Bootstrapper requestBootstrapper = request.getBootstrapper();
            request.setBootstrapper(new ChainBootstrapper(new ServerBootstrapper(), requestBootstrapper));

            context.push();
            CommandEventOutputStream outputStream = null;
            try
            {
                recipeCleanup.cleanup(eventManager, processorPaths.getRecipesRoot(), request.getId());

                context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, processorPaths);
                context.addValue(NAMESPACE_INTERNAL, PROPERTY_RESOURCE_REPOSITORY, repo);
                context.addValue(NAMESPACE_INTERNAL, PROPERTY_FILE_REPOSITORY, new SlaveFileRepository(processorPaths.getRecipeRoot(), master, serviceTokenManager));
                context.addValue(NAMESPACE_INTERNAL, PROPERTY_PATCH_FORMAT_FACTORY, patchFormatFactory);
                context.addValue(NAMESPACE_INTERNAL, PROPERTY_SCM_CLIENT_FACTORY, scmClientFactory);
                outputStream = new CommandEventOutputStream(eventManager, request.getId());
                context.setOutputStream(outputStream);
                context.setWorkingDir(processorPaths.getBaseDir());

                recipeProcessor.build(request);
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
                IOUtils.close(outputStream);
                context.pop();
                eventManager.unregister(listener);
                buildingRecipe.set(SlaveStatus.NO_RECIPE);
            }
        }
    }

    public long getBuildingRecipe()
    {
        return buildingRecipe.get();
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

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    public void setScmClientFactory(ScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }

    public void setPatchFormatFactory(PatchFormatFactory patchFormatFactory)
    {
        this.patchFormatFactory = patchFormatFactory;
    }
}
