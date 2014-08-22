package com.zutubi.pulse.master;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.patch.PatchFormatFactory;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.repository.MasterFileRepository;
import com.zutubi.pulse.servercore.*;
import com.zutubi.util.io.FileSystem;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;

/**
 * A runnable that wraps execution of a recipe on the master.
 */
public class MasterRecipeRunner implements RecipeRunner
{
    private static final Logger LOG = Logger.getLogger(MasterRecipeRunner.class);

    private EventManager eventManager;
    private MasterConfigurationManager configurationManager;
    private ResourceRepository resourceRepository;
    private RecipeCleanup recipeCleanup;
    private ScmClientFactory scmClientFactory;
    private PatchFormatFactory patchFormatFactory;

    public MasterRecipeRunner(ResourceRepository resourceRepository)
    {
        this.resourceRepository = resourceRepository;
        recipeCleanup = new RecipeCleanup(new FileSystem());
    }

    public void runRecipe(RecipeRequest request, RecipeProcessor recipeProcessor)
    {
        Bootstrapper requestBootstrapper = request.getBootstrapper();
        request.setBootstrapper(new ChainBootstrapper(new ServerBootstrapper(), requestBootstrapper));

        PulseExecutionContext context = request.getContext();
        long buildId = context.getLong(NAMESPACE_INTERNAL, PROPERTY_BUILD_ID, 0);
        File dataDir = configurationManager.getUserPaths().getData();
        ServerRecipePaths recipePaths = new ServerRecipePaths(context, dataDir);

        EventOutputStream outputStream = null;
        context.push();
        try
        {
            recipeCleanup.cleanup(eventManager, recipePaths.getRecipesRoot(), buildId, request.getId());

            context.addValue(NAMESPACE_INTERNAL, PROPERTY_DATA_DIR, dataDir.getAbsolutePath());
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, recipePaths);
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_RESOURCE_REPOSITORY, resourceRepository);
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_FILE_REPOSITORY, new MasterFileRepository(configurationManager));
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_PATCH_FORMAT_FACTORY, patchFormatFactory);
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_SCM_CLIENT_FACTORY, scmClientFactory);
            if (context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_ENABLE_LIVE_LOGS, true))
            {
                outputStream = new EventOutputStream(eventManager, true, buildId, request.getId());
                context.setOutputStream(outputStream);
            }
            context.setWorkingDir(recipePaths.getBaseDir());

            recipeProcessor.build(request);
        }
        catch (BuildException e)
        {
            eventManager.publish(new RecipeErrorEvent(this, buildId, request.getId(), e.getMessage(), false));
        }
        catch (Exception e)
        {
            LOG.severe(e);
            eventManager.publish(new RecipeErrorEvent(this, buildId, request.getId(), "Unexpected error: " + e.getMessage(), false));
        }
        finally
        {
            IOUtils.close(outputStream);
            context.pop();
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
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
