package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.*;
import static com.zutubi.pulse.core.BuildProperties.*;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.repository.MasterFileRepository;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

/**
 *
 */
public class MasterRecipeRunner implements Runnable
{
    private static final Logger LOG = Logger.getLogger(MasterRecipeRunner.class);

    private RecipeRequest request;
    private RecipeProcessor recipeProcessor;
    private EventManager eventManager;
    private MasterConfigurationManager configurationManager;
    private ResourceRepository resourceRepository;

    public MasterRecipeRunner(RecipeRequest request, RecipeProcessor recipeProcessor, EventManager eventManager, MasterConfigurationManager configurationManager, ResourceRepository resourceRepository)
    {
        this.request = request;
        this.recipeProcessor = recipeProcessor;
        this.eventManager = eventManager;
        this.configurationManager = configurationManager;
        this.resourceRepository = resourceRepository;
    }

    public void run()
    {
        Bootstrapper requestBootstrapper = request.getBootstrapper();
        request.setBootstrapper(new ChainBootstrapper(new ServerBootstrapper(), requestBootstrapper));

        ExecutionContext context = request.getContext();
        ServerRecipePaths recipePaths = new ServerRecipePaths(request.getProject(), request.getId(), configurationManager.getUserPaths().getData(), context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_INCREMENTAL_BUILD, false));

        EventOutputStream outputStream = null;
        context.push();
        try
        {
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, recipePaths);
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_RESOURCE_REPOSITORY, resourceRepository);
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_FILE_REPOSITORY, new MasterFileRepository(configurationManager));
            outputStream = new CommandEventOutputStream(eventManager, request.getId(), true);
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

}
