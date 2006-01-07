package com.cinnamonbob;

import com.cinnamonbob.core.event.AsynchronousDelegatingListener;
import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.events.build.BuildRequestEvent;
import com.cinnamonbob.model.BuildManager;
import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.util.logging.Logger;

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

        RecipeResultCollector collector = new DefaultRecipeResultCollector(project);
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
