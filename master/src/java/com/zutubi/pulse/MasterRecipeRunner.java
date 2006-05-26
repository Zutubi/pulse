/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.util.logging.Logger;

/**
 */
public class MasterRecipeRunner implements Runnable
{
    private static final Logger LOG = Logger.getLogger(MasterRecipeRunner.class);

    private RecipeRequest request;
    private RecipeProcessor recipeProcessor;
    private EventManager eventManager;
    private ConfigurationManager configurationManager;

    public MasterRecipeRunner(RecipeRequest request, RecipeProcessor recipeProcessor, EventManager eventManager, ConfigurationManager configurationManager)
    {
        this.request = request;
        this.recipeProcessor = recipeProcessor;
        this.eventManager = eventManager;
        this.configurationManager = configurationManager;
    }

    public void run()
    {
        Bootstrapper bootstrapper = new ChainBootstrapper(new ServerBootstrapper(), request.getBootstrapper());
        ServerRecipePaths recipePaths = new ServerRecipePaths(request.getId(), configurationManager.getUserPaths().getData());

        try
        {
            recipeProcessor.build(request.getId(), recipePaths, bootstrapper, request.getPulseFileSource(), request.getRecipeName());
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
