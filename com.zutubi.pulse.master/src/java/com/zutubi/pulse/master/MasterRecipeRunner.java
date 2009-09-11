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
import com.zutubi.util.FileSystem;
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
        long agentHandle = context.getLong(NAMESPACE_INTERNAL, PROPERTY_AGENT_HANDLE, 0);
        String agentName = context.getString(NAMESPACE_INTERNAL, PROPERTY_AGENT);
        String agentDataPattern = context.getString(NAMESPACE_INTERNAL, PROPERTY_AGENT_DATA_PATTERN);
        long projectHandle = context.getLong(NAMESPACE_INTERNAL, PROPERTY_PROJECT_HANDLE, 0);
        File dataDir = configurationManager.getUserPaths().getData();
        boolean incremental = context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_INCREMENTAL_BUILD, false);
        String projectPersistentPattern = context.getString(NAMESPACE_INTERNAL, PROPERTY_PERSISTENT_WORK_PATTERN);
        ServerRecipePaths recipePaths = new ServerRecipePaths(agentHandle, agentName, agentDataPattern, projectHandle, request.getProject(), request.getId(), incremental, projectPersistentPattern, dataDir);

        CommandEventOutputStream outputStream = null;
        context.push();
        try
        {
            recipeCleanup.cleanup(eventManager, recipePaths.getRecipesRoot(), request.getId());

            context.addValue(NAMESPACE_INTERNAL, PROPERTY_DATA_DIR, dataDir.getAbsolutePath());
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, recipePaths);
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_RESOURCE_REPOSITORY, resourceRepository);
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_FILE_REPOSITORY, new MasterFileRepository(configurationManager));
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_PATCH_FORMAT_FACTORY, patchFormatFactory);
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_SCM_CLIENT_FACTORY, scmClientFactory);
            outputStream = new CommandEventOutputStream(eventManager, request.getId());
            context.setOutputStream(outputStream);
            context.setWorkingDir(recipePaths.getBaseDir());

            recipeProcessor.build(request);
        }
        catch (BuildException e)
        {
            eventManager.publish(new RecipeErrorEvent(this, request.getId(), e.getMessage()));
        }
        catch (Exception e)
        {
            LOG.severe(e);
            eventManager.publish(new RecipeErrorEvent(this, request.getId(), "Unexpected error: " + e.getMessage()));
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
