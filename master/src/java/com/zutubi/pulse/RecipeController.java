package com.zutubi.pulse;

import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.FeaturePersister;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.events.build.*;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.util.logging.Logger;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public class RecipeController
{
    private static final Logger LOG = Logger.getLogger(RecipeController.class);

    private BuildResult buildResult;
    private RecipeResultNode recipeResultNode;
    private RecipeResult recipeResult;
    private RecipeDispatchRequest dispatchRequest;
    private boolean incremental;
    private RecipeResultNode previousSuccessful;
    private RecipeLogger logger;
    private RecipeResultCollector collector;
    private BuildManager buildManager;
    private ServiceTokenManager serviceTokenManager;
    private boolean finished = false;

    private RecipeQueue queue;
    private AgentService agentService;
    private EventManager eventManager;

    public RecipeController(BuildResult buildResult, RecipeResultNode recipeResultNode, RecipeDispatchRequest dispatchRequest, boolean incremental, RecipeResultNode previousSuccessful, RecipeLogger logger, RecipeResultCollector collector)
    {
        this.buildResult = buildResult;
        this.recipeResultNode = recipeResultNode;
        this.recipeResult = recipeResultNode.getResult();
        this.dispatchRequest = dispatchRequest;
        this.incremental = incremental;
        this.previousSuccessful = previousSuccessful;
        this.logger = logger;
        this.collector = collector;
    }

    public void prepare(BuildResult buildResult)
    {
        // Errors handled by BuildController
        collector.prepare(buildResult, recipeResultNode.getResult().getId());
        logger.prepare();
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
            else if(event instanceof CommandOutputEvent)
            {
                handleCommandOutput((CommandOutputEvent)event);
            }
            else if (event instanceof CommandCompletedEvent)
            {
                handleCommandCompleted((CommandCompletedEvent) event);
            }
            else if (event instanceof RecipeCompletedEvent)
            {
                handleRecipeCompleted((RecipeCompletedEvent) event);
            }
            else if (event instanceof RecipeStatusEvent)
            {
                logger.log((RecipeStatusEvent) event);
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
        logger.log(event);
        agentService = event.getAgent().getService();
        recipeResultNode.setHost(agentService.getHostName());
        buildManager.save(recipeResultNode);
    }

    private void handleRecipeCommenced(RecipeCommencedEvent event)
    {
        recipeResult.commence(event.getName(), System.currentTimeMillis());
        if(previousSuccessful != null)
        {
            RecipeResult result = previousSuccessful.getResult();
            if(result != null)
            {
                recipeResult.getStamps().setEstimatedRunningTime(result.getStamps().getElapsed());
            }
        }

        if (recipeResult.terminating())
        {
            // This terminate must have come in before the recipe commenced.
            // Now we know who to tell to stop processing the recipe!
            agentService.terminateRecipe(recipeResult.getId());
        }
        buildManager.save(recipeResult);
        logger.log(event, recipeResult);
    }

    private void handleCommandCommenced(CommandCommencedEvent event)
    {
        CommandResult result = new CommandResult(event.getName());
        if(previousSuccessful != null)
        {
            RecipeResult previousResult = previousSuccessful.getResult();
            if(previousResult != null)
            {
                CommandResult previousCommand = previousResult.getCommandResult(event.getName());
                if(previousCommand != null)
                {
                    result.getStamps().setEstimatedRunningTime(previousCommand.getStamps().getElapsed());
                }
            }
        }
        result.commence(System.currentTimeMillis());
        recipeResult.add(result);
        buildManager.save(recipeResult);
        logger.log(event, result);
    }

    private void handleCommandOutput(CommandOutputEvent event)
    {
        logger.log(event);
    }

    private void handleCommandCompleted(CommandCompletedEvent event)
    {
        CommandResult result = event.getResult();
        result.getStamps().setEndTime(System.currentTimeMillis());
        FeaturePersister persister = new FeaturePersister();
        try
        {
            persister.writeFeatures(result, collector.getRecipeDir(buildResult, recipeResult.getId()));
        }
        catch (IOException e)
        {
            LOG.severe("Unable to save features for command '" + result.getCommandName() + "': " + e.getMessage(), e);
        }
        
        recipeResult.update(result);
        buildManager.save(recipeResult);
        logger.log(event, result);
    }

    private void handleRecipeCompleted(RecipeCompletedEvent event)
    {
        RecipeResult result = event.getResult();
        result.getStamps().setEndTime(System.currentTimeMillis());
        recipeResult.update(event.getResult());
        logger.log(event, recipeResult);
        complete();
    }

    private void handleRecipeError(RecipeErrorEvent event)
    {
        recipeResult.error(event.getErrorMessage());
        logger.log(event, recipeResult);
        complete();
    }

    private void complete()
    {
        recipeResult.complete();
        recipeResult.abortUnfinishedCommands();

        eventManager.publish(new PostStageEvent(this, buildResult, recipeResultNode));

        buildManager.save(recipeResult);
        logger.complete(recipeResult);
        finished = true;
    }

    public boolean isFinished()
    {
        return finished;
    }

    public void collect(BuildResult buildResult, boolean collectWorkingCopy)
    {
        try
        {
            logger.collecting(recipeResult, collectWorkingCopy);
            collector.collect(buildResult, recipeResult.getId(), collectWorkingCopy, incremental, agentService);
        }
        catch (BuildException e)
        {
            handleBuildException(e);
        }
        catch (Exception e)
        {
            handleUnexpectedException(e);
        }
        finally
        {
            logger.collectionComplete();
        }
    }

    public void cleanup(BuildResult buildResult)
    {
        try
        {
            collector.cleanup(buildResult, recipeResult.getId(), incremental, agentService);
        }
        catch (Exception e)
        {
            LOG.warning("Unable to clean up recipe '" + recipeResult.getId() + "'", e);
        }
    }

    public Bootstrapper getChildBootstrapper()
    {
        // use the service details to configure the copy bootstrapper.
        String url = agentService.getUrl();
        return new CopyBootstrapper(url, serviceTokenManager.getToken(), recipeResult.getId());
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

    public void terminateRecipe(boolean timeout)
    {
        if (recipeResult.commenced())
        {
            // Tell the build service that it can stop trying to execute this
            // recipe.  We *must* have received the commenced event before we
            // can do this.
            recipeResult.terminate(timeout);
            agentService.terminateRecipe(recipeResult.getId());
        }
        else
        {
            // Not yet commanced, try and catch it at the recipe queue. If
            // we don't catch it, then we wait for the RecipeCommencedEvent.
            recipeResult.terminate(timeout);
            if(queue.cancelRequest(recipeResult.getId()))
            {
                // We caught it now, so we are complete.
                complete();
            }
        }

    }

    public RecipeDispatchRequest getDispatchRequest()
    {
        return dispatchRequest;
    }

    public RecipeResult getResult()
    {
        return recipeResult;
    }


    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    public void setRecipeQueue(RecipeQueue queue)
    {
        this.queue = queue;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
