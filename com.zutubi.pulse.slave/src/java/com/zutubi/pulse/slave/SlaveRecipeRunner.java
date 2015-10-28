package com.zutubi.pulse.slave;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.patch.PatchFormatFactory;
import com.zutubi.pulse.servercore.*;
import com.zutubi.pulse.servercore.services.MasterService;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.pulse.slave.repository.SlaveFileRepository;
import com.zutubi.util.io.FileSystem;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.OutputStream;
import java.net.MalformedURLException;

import static com.zutubi.pulse.core.engine.api.BuildProperties.*;

/**
 * Runner for recipes executing on a slave service.
 */
public class SlaveRecipeRunner implements RecipeRunner
{
    private static final Logger LOG = Logger.getLogger(SlaveRecipeRunner.class);

    private static final String PROPERTY_LIVE_LOGGING_ENABLED = "live.logging.enabled";

    private String master;
    private SlaveConfigurationManager configurationManager;
    private EventManager eventManager;
    private MasterProxyFactory masterProxyFactory;
    private ServiceTokenManager serviceTokenManager;
    private PatchFormatFactory patchFormatFactory;
    private ScmClientFactory scmClientFactory;
    private RecipeCleanup recipeCleanup;

    public SlaveRecipeRunner(String master)
    {
        this.master = master;
        recipeCleanup = new RecipeCleanup(new FileSystem());
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

    public void runRecipe(RecipeRequest request, RecipeProcessor recipeProcessor)
    {
        MasterService masterProxy = getMasterProxy(master);
        if (masterProxy != null)
        {
            PulseExecutionContext context = request.getContext();
            long buildId = context.getLong(NAMESPACE_INTERNAL, PROPERTY_BUILD_ID, 0);
            AgentRecipeDetails details = new AgentRecipeDetails(context);
            File dataDir = configurationManager.getUserPaths().getData();
            ServerRecipePaths processorPaths = new ServerRecipePaths(context, dataDir);

            ResourceRepository repo = new RemoteResourceRepository(details.getAgentHandle(), masterProxy, serviceTokenManager);

            Bootstrapper requestBootstrapper = request.getBootstrapper();
            request.setBootstrapper(new ChainBootstrapper(new ServerBootstrapper(), requestBootstrapper));

            context.push();
            OutputStream outputStream = null;
            try
            {
                recipeCleanup.cleanup(eventManager, processorPaths.getRecipesRoot(), buildId, request.getId());

                context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, processorPaths);
                context.addValue(NAMESPACE_INTERNAL, PROPERTY_RESOURCE_REPOSITORY, repo);
                context.addValue(NAMESPACE_INTERNAL, PROPERTY_FILE_REPOSITORY, new SlaveFileRepository(processorPaths.getRecipeRoot(), master, serviceTokenManager));
                context.addValue(NAMESPACE_INTERNAL, PROPERTY_PATCH_FORMAT_FACTORY, patchFormatFactory);
                context.addValue(NAMESPACE_INTERNAL, PROPERTY_SCM_CLIENT_FACTORY, scmClientFactory);
                if (isLiveLoggingEnabled(context))
                {
                    outputStream = new EventOutputStream(eventManager, true, buildId, request.getId());
                    context.setOutputStream(outputStream);
                }
                context.setWorkingDir(processorPaths.getBaseDir());

                recipeProcessor.build(request);
            }
            catch (BuildException e)
            {
                LOG.warning("A problem occurred while processing a recipe build request. Reason: " + e.getMessage(), e);
                RecipeErrorEvent error = new RecipeErrorEvent(null, buildId, request.getId(), e.getMessage(), false);
                eventManager.publish(error);
            }
            catch (Exception e)
            {
                LOG.severe(e);
                RecipeErrorEvent error = new RecipeErrorEvent(null, buildId, request.getId(), "Unexpected error: " + e.getMessage(), false);
                eventManager.publish(error);
            }
            finally
            {
                IOUtils.close(outputStream);
                context.pop();
            }
        }
    }

    private boolean isLiveLoggingEnabled(PulseExecutionContext context)
    {
        if (context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_ENABLE_LIVE_LOGS, true))
        {
            String propertyValue = System.getProperty(PROPERTY_LIVE_LOGGING_ENABLED);
            return propertyValue == null || Boolean.parseBoolean(propertyValue);
        }
        else
        {
            return false;
        }
    }

    public void setConfigurationManager(SlaveConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
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
