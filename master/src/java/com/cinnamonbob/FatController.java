package com.cinnamonbob;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.event.AsynchronousDelegatingListener;
import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.events.build.*;
import com.cinnamonbob.model.*;
import com.cinnamonbob.scm.SCMException;
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
    // TODO obviously a hack, to be replaced with all build/recipe invocations in progress
    private BuildSpecification runningSpec;
    private BuildResult runningBuild;
    private BuildService runningService;

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
        else if (event instanceof RecipeDispatchedEvent)
        {
            handleRecipeDispatch((RecipeDispatchedEvent) event);
        }
        else
        {
            handleRecipeEvent((RecipeEvent) event);
        }
    }

    private void handleRecipeDispatch(RecipeDispatchedEvent event)
    {
        // TODO set the host on the result node
        runningService = event.getService();
    }

    private void handleBuildRequest(BuildRequestEvent event)
    {
        String specName = event.getSpecification();
        Project project = event.getProject();
        BuildSpecification buildSpec = project.getBuildSpecification(specName);

        if (buildSpec == null)
        {
            LOG.warning("Request to build unknown specification '" + specName + "' for project '" + project.getName() + "'");
            return;
        }

        runningSpec = buildSpec;

        long number = buildManager.getNextBuildNumber(project);

        BuildResult buildResult = new BuildResult(project, number);
        runningBuild = buildResult;

        MasterBuildPaths paths = new MasterBuildPaths();
        File buildDir = paths.getBuildDir(project, buildResult);

        buildResult.commence(buildDir);
        eventManager.publish(new BuildCommencedEvent(this, buildResult));

        try
        {
            if (!buildDir.mkdirs())
            {
                throw new BuildException("Unable to create build directory '" + buildDir.getAbsolutePath() + "'");
            }

            ScmBootstrapper bootstrapper = createBuildBootstrapper(project);

            executeNode(project, buildResult, buildSpec.getRoot().getChildren().get(0), bootstrapper);
        }
        catch (BuildException e)
        {
            LOG.severe("Build error", e);
            buildResult.error(e);
        }
    }

    private void executeNode(Project project, BuildResult buildResult, BuildSpecificationNode node, ScmBootstrapper bootstrapper)
    {
        RecipeResult result = new RecipeResult(node.getRecipe());
        RecipeResultNode resultNode = new RecipeResultNode(result);

        // TODO: not this simple: need to know where to add it
        buildResult.add(resultNode);
        // Make sure the recipe result gets an id
        buildManager.save(buildResult);

        MasterBuildPaths paths = new MasterBuildPaths();
        File recipeDir = paths.getRecipeDir(project, buildResult, result.getId());

        if (!recipeDir.mkdirs())
        {
            throw new BuildException("Could not create recipe directory '" + recipeDir.getAbsolutePath() + "'");
        }

        RecipeRequest request = new RecipeRequest(result.getId(), bootstrapper, project.getBobFile(), node.getRecipe());
        RecipeDispatchRequest dispatchRequest = new RecipeDispatchRequest(node.getHostRequirements(), request);
        recipeQueue.enqueue(dispatchRequest);
    }

    public ScmBootstrapper createBuildBootstrapper(Project project)
    {
        ScmBootstrapper bootstrapper = new ScmBootstrapper();

        for (Scm scm : project.getScms())
        {
            try
            {
                Revision revision = scm.createServer().getLatestRevision();
                bootstrapper.add(new ScmCheckoutDetails(scm, revision));
            }
            catch (SCMException e)
            {
                throw new BuildException("Could not retrieve latest revision from SCM '" + scm.getName() + "'", e);
            }
        }

        return bootstrapper;
    }

    private void handleRecipeEvent(RecipeEvent event)
    {
        // TODO blatantly assumes one recipe only from runningSpec
        // TODO oh, the humanity (and I don't mean the ground crew)
        RecipeResult result = event.getResult();

        if (result.getId() != runningBuild.getResults().get(0).getResult().getId())
        {
            LOG.severe("i didn't expect that!");
        }

        runningBuild.getResults().get(0).getResult().update(result);

        // complete?
        if (event instanceof RecipeCompletedEvent)
        {
            // retrieve recipe output.
            long recipeId = event.getResult().getId();
            MasterBuildPaths paths = new MasterBuildPaths();
            File outputDir = paths.getOutputDir(runningBuild.getProject(), runningBuild, recipeId);

            if (!outputDir.mkdirs())
            {
                // TODO throw something, but where to handle it (need to locate which result to apply to...)
            }

            runningService.collectResults(recipeId, outputDir);
            runningService.cleanupResults(recipeId);

            // TODO if the whole build is done...
            runningBuild.complete();
            eventManager.publish(new BuildCompletedEvent(this, runningBuild));
        }

        buildManager.save(runningBuild);
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildRequestEvent.class, RecipeDispatchedEvent.class, RecipeEvent.class};
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

    private class BuildInvocation
    {
        public BuildResult result;
        public int pendingRecipes;

        public BuildInvocation(BuildResult result)
        {
            this.result = result;
            pendingRecipes = 0;
        }
    }

    private class RecipeInvocation
    {
        public BuildInvocation parent;
        public RecipeResult result;
        public BuildSpecificationNode node;
    }
}
