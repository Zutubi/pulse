package com.cinnamonbob;

import com.cinnamonbob.core.Bootstrapper;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.events.build.*;
import com.cinnamonbob.model.BuildManager;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.RecipeResultNode;
import com.cinnamonbob.util.logging.Logger;

/**
 *
 */
public class RecipeController
{
    private static final Logger LOG = Logger.getLogger(RecipeController.class);

    private RecipeResultNode recipeResultNode;
    private RecipeResult recipeResult;
    private RecipeDispatchRequest dispatchRequest;
    private RecipeResultCollector collector;
    private BuildManager buildManager;
    private boolean finished = false;

    private RecipeQueue queue;
    private BuildService buildService;

    public RecipeController(RecipeResultNode recipeResultNode, RecipeDispatchRequest dispatchRequest, RecipeResultCollector collector, RecipeQueue queue, BuildManager manager)
    {
        this.recipeResultNode = recipeResultNode;
        this.recipeResult = recipeResultNode.getResult();
        this.dispatchRequest = dispatchRequest;
        this.collector = collector;
        this.queue = queue;
        this.buildManager = manager;
    }

    public void prepare(BuildResult buildResult)
    {
        // Errors handled by BuildController
        collector.prepare(buildResult, recipeResultNode.getResult().getId());
    }

    public void initialise(Bootstrapper bootstrapper)
    {
        try
        {
            // allow for just in time setting of the bootstrapper since this can not be configured during
            // the build initialisation.
            dispatchRequest.getRequest().setBootstrapper(bootstrapper);
            queue.enqueue(dispatchRequest);
        }
        catch (BuildException e)
        {
            handleBuildException(e);
        }
        catch (Exception e)
        {
            handleUnexpectedException(e);
        }
    }

    /**
     * @param event
     */
    public boolean handleRecipeEvent(RecipeEvent event)
    {
        if (event.getRecipeId() != recipeResult.getId())
        {
            // not interested in this event..
            return false;
        }

        try
        {
            if (event instanceof RecipeDispatchedEvent)
            {
                handleRecipeDispatch((RecipeDispatchedEvent) event);
            }
            else if (event instanceof RecipeCommencedEvent)
            {
                handleRecipeCommenced((RecipeCommencedEvent) event);
            }
            else if (event instanceof CommandCommencedEvent)
            {
                handleCommandCommenced((CommandCommencedEvent) event);
            }
            else if (event instanceof CommandCompletedEvent)
            {
                handleCommandCompleted((CommandCompletedEvent) event);
            }
            else if (event instanceof RecipeCompletedEvent)
            {
                handleRecipeCompleted((RecipeCompletedEvent) event);
            }
            else if (event instanceof RecipeErrorEvent)
            {
                handleRecipeError((RecipeErrorEvent) event);
            }
        }
        catch (BuildException e)
        {
            handleBuildException(e);
        }
        catch (Exception e)
        {
            handleUnexpectedException(e);
        }

        return true;
    }

    private void handleRecipeDispatch(RecipeDispatchedEvent event)
    {
        buildService = event.getService();
        recipeResultNode.setHost(buildService.getHostName());
        buildManager.save(recipeResultNode);
    }

    private void handleRecipeCommenced(RecipeCommencedEvent event)
    {
        recipeResult.commence(event.getName(), event.getStartTime());
        buildManager.save(recipeResult);
    }

    private void handleCommandCommenced(CommandCommencedEvent event)
    {
        CommandResult result = new CommandResult(event.getName());
        result.commence(event.getStartTime());
        recipeResult.add(result);
        buildManager.save(recipeResult);
    }

    private void handleCommandCompleted(CommandCompletedEvent event)
    {
        recipeResult.update(event.getResult());
        buildManager.save(recipeResult);
    }

    private void handleRecipeCompleted(RecipeCompletedEvent event)
    {
        recipeResult.update(event.getResult());
        complete();
    }

    private void handleRecipeError(RecipeErrorEvent event)
    {
        recipeResult.error(event.getErrorMessage());
        complete();
    }

    private void complete()
    {
        recipeResult.complete();
        recipeResult.abortUnfinishedCommands();
        buildManager.save(recipeResult);
        finished = true;
    }

    public boolean isFinished()
    {
        return finished;
    }

    public void collect(BuildResult buildResult)
    {
        try
        {
            collector.collect(buildResult, recipeResult.getId(), buildService);
        }
        catch (BuildException e)
        {
            handleBuildException(e);
        }
        catch (Exception e)
        {
            handleUnexpectedException(e);
        }
    }

    public void cleanup(BuildResult buildResult)
    {
        try
        {
            collector.cleanup(buildResult, recipeResult.getId(), buildService);
        }
        catch (Exception e)
        {
            LOG.warning("Unable to clean up recipe '" + recipeResult.getId() + "'", e);
        }
    }

    public Bootstrapper getChildBootstrapper()
    {
        // use the service details to configure the copy bootstrapper.
        String url = buildService.getUrl();
        return new CopyBootstrapper(url, recipeResult.getId());
    }

    public boolean succeeded()
    {
        return recipeResult.succeeded();
    }

    public String getRecipeName()
    {
        return recipeResultNode.getResult().getRecipeNameSafe();
    }

    public String getRecipeHost()
    {
        return recipeResultNode.getHostSafe();
    }

    private void handleBuildException(BuildException e)
    {
        recipeResult.error(e);
        complete();
    }

    private void handleUnexpectedException(Exception e)
    {
        LOG.severe(e);
        recipeResult.error("Unexpected error: " + e.getMessage());
        complete();
    }

}
