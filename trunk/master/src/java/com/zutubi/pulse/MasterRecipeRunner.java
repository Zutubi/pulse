package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.repository.MasterFileRepository;
import com.zutubi.pulse.util.logging.Logger;

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
    private BuildContext context;

    public MasterRecipeRunner(RecipeRequest request, RecipeProcessor recipeProcessor, EventManager eventManager, MasterConfigurationManager configurationManager, ResourceRepository resourceRepository, BuildContext context)
    {
        this.request = request;
        this.recipeProcessor = recipeProcessor;
        this.eventManager = eventManager;
        this.configurationManager = configurationManager;
        this.resourceRepository = resourceRepository;
        this.context = context;
    }

    public void run()
    {
        Bootstrapper requestBootstrapper = request.getBootstrapper();

        context.setFileRepository(new MasterFileRepository(configurationManager));
        request.setBootstrapper(new ChainBootstrapper(new ServerBootstrapper(), requestBootstrapper));
        ServerRecipePaths recipePaths = new ServerRecipePaths(request.getProject(), request.getSpec(), request.getId(), configurationManager.getUserPaths().getData(), request.isIncremental());

        try
        {
            recipeProcessor.build(request, recipePaths, resourceRepository, true, context);
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
    }

}
