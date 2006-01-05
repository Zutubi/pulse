package com.cinnamonbob;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.event.AsynchronousDelegatingListener;
import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.events.build.BuildRequestEvent;
import com.cinnamonbob.model.BuildManager;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.util.logging.Logger;

import java.io.File;

/**
 * A FatController coordinates execution of a build specification, gathering
 * of the results, and so on.  It ensures proper ordering of the build steps.
 */
public class FatController implements EventListener
{
    private static final Logger LOG = Logger.getLogger(FatController.class);

    private EventManager eventManager;
    private AsynchronousDelegatingListener asyncListener;
    private BuildManager buildManager;
    private RecipeQueue recipeQueue;

    public FatController()
    {

    }

    public void init()
    {
        asyncListener = new AsynchronousDelegatingListener(this);
        eventManager.register(asyncListener);
    }

    public void stop()
    {
        eventManager.unregister(asyncListener);
        asyncListener.stop();
    }

    public void handleEvent(Event event)
    {
        if (event instanceof BuildRequestEvent)
        {
            handleBuildRequest((BuildRequestEvent) event);
        }
    }

    private void handleBuildRequest(BuildRequestEvent event)
    {
        final Project project = event.getProject();
        String specName = event.getSpecification();

        BuildSpecification buildSpec = project.getBuildSpecification(specName);
        if (buildSpec == null)
        {
            LOG.warning("Request to build unknown specification '" + specName + "' for project '" + project.getName() + "'");
            return;
        }

        final MasterBuildPaths paths = new MasterBuildPaths();

        RecipeResultCollector collector = new RecipeResultCollector()
        {
            public void prepare(BuildResult result, long recipeId)
            {
                // ensure that we have created the necessary directories.
                File recipeDir = paths.getRecipeDir(project, result, recipeId);
                if (!recipeDir.mkdirs())
                {
                    throw new BuildException("Failed to create the '" + recipeDir + "' directory.");
                }
            }

            public void collect(BuildResult result, long recipeId, BuildService buildService)
            {
                buildService.collectResults(recipeId, paths.getRecipeDir(project, result, recipeId));
            }

            public void cleanup(BuildResult result, long recipeId, BuildService buildService)
            {
                buildService.cleanup(recipeId);
            }
        };

        BuildController controller = new BuildController(project, buildSpec, eventManager, buildManager, recipeQueue, collector);
        controller.run();
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildRequestEvent.class};
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setRecipeQueue(RecipeQueue recipeQueue)
    {
        this.recipeQueue = recipeQueue;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
