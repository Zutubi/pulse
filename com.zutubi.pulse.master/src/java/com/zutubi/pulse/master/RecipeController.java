package com.zutubi.pulse.master;

import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import static com.zutubi.pulse.core.BuildProperties.*;
import com.zutubi.pulse.core.events.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.FeaturePersister;
import com.zutubi.pulse.core.model.RecipeResult;
import static com.zutubi.pulse.master.MasterBuildProperties.addRevisionProperties;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.events.build.PostStageEvent;
import com.zutubi.pulse.master.events.build.RecipeAssignedEvent;
import com.zutubi.pulse.master.events.build.RecipeDispatchedEvent;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookManager;
import com.zutubi.pulse.servercore.CopyBootstrapper;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.util.logging.Logger;

import java.io.IOException;

/**
 *
 */
public class RecipeController
{
    private static final Logger LOG = Logger.getLogger(RecipeController.class);

    private BuildResult buildResult;
    private RecipeResultNode recipeResultNode;
    private RecipeResult recipeResult;
    private RecipeAssignmentRequest assignmentRequest;
    private PulseExecutionContext recipeContext;
    private RecipeResultNode previousSuccessful;
    private RecipeLogger logger;
    private RecipeResultCollector collector;
    private BuildManager buildManager;
    private ServiceTokenManager serviceTokenManager;
    private boolean finished = false;

    private RecipeQueue recipeQueue;
    private AgentService agentService;
    private EventManager eventManager;
    private MasterConfigurationManager configurationManager;
    private ResourceManager resourceManager;
    private BuildHookManager buildHookManager;
    private RecipeDispatchService recipeDispatchService;

    public RecipeController(BuildResult buildResult, RecipeResultNode recipeResultNode, RecipeAssignmentRequest assignmentRequest, PulseExecutionContext recipeContext, RecipeResultNode previousSuccessful, RecipeLogger logger, RecipeResultCollector collector, MasterConfigurationManager configurationManager, ResourceManager resourceManager, RecipeDispatchService recipeDispatchService)
    {
        this.buildResult = buildResult;
        this.recipeResultNode = recipeResultNode;
        this.recipeResult = recipeResultNode.getResult();
        this.assignmentRequest = assignmentRequest;
        this.recipeContext = recipeContext;
        this.previousSuccessful = previousSuccessful;
        this.logger = logger;
        this.collector = collector;
        this.configurationManager = configurationManager;
        this.resourceManager = resourceManager;
        this.recipeDispatchService = recipeDispatchService;
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
            assignmentRequest.getRequest().setBootstrapper(bootstrapper);
            recipeQueue.enqueue(assignmentRequest);
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

    public boolean matchesRecipeEvent(RecipeEvent event)
    {
        return event.getRecipeId() == recipeResult.getId();
    }

    public void handleRecipeEvent(RecipeEvent event)
    {
        try
        {
            if (event instanceof RecipeAssignedEvent)
            {
                handleRecipeAssigned((RecipeAssignedEvent) event);
            }
            else if (event instanceof RecipeDispatchedEvent)
            {
                handleRecipeDispatched((RecipeDispatchedEvent) event);
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
    }

    private void handleRecipeAssigned(RecipeAssignedEvent event)
    {
        logger.log(event);
        Agent agent = event.getAgent();
        agentService = agent.getService();
        recipeResultNode.setHost(agentService.getHostName());
        buildManager.save(recipeResultNode);

        ResourceRepository resourceRepository = resourceManager.getAgentRepository(agent);
        if (resourceRepository != null)
        {
            BuildProperties.addResourceProperties(recipeContext, assignmentRequest.getResourceRequirements(), resourceRepository);
        }

        // Update the request and its context before it is sent to the agent.
        RecipeRequest recipeRequest = assignmentRequest.getRequest();
        BuildRevision buildRevision = assignmentRequest.getRevision();
        recipeRequest.setPulseFileSource(buildRevision.getPulseFile());

        ExecutionContext agentContext = recipeRequest.getContext();
        addRevisionProperties(agentContext, buildRevision);
        agentContext.addString(NAMESPACE_INTERNAL, BuildProperties.PROPERTY_AGENT, agent.getConfig().getName());
        agentContext.addString(NAMESPACE_INTERNAL, PROPERTY_CLEAN_BUILD, Boolean.toString(buildResult.getProject().isForceCleanForAgent(agent.getId())));

        // Now it may be dispatched.
        recipeDispatchService.dispatch(event);
    }

    private void handleRecipeDispatched(RecipeDispatchedEvent event)
    {
        logger.log(event);
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
        logger.log(event.getData());
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
        result.setOutputDir("dir");
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

        MasterBuildProperties.addStageProperties(recipeContext, buildResult, recipeResultNode, configurationManager, false);

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
            collector.collect(buildResult, recipeResult.getId(), collectWorkingCopy, recipeContext.getBoolean(NAMESPACE_INTERNAL, PROPERTY_INCREMENTAL_BUILD, false), agentService);
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
            logger.cleaning();
            collector.cleanup(buildResult, recipeResult.getId(), recipeContext.getBoolean(NAMESPACE_INTERNAL, PROPERTY_INCREMENTAL_BUILD, false), agentService);
        }
        catch (Exception e)
        {
            LOG.warning("Unable to clean up recipe '" + recipeResult.getId() + "'", e);
        }
        finally
        {
            logger.cleaningComplete();
        }
    }

    public void sendPostStageEvent()
    {
        try
        {
            logger.postStage();
            try
            {
                publishEvent(new PostStageEvent(this, buildResult, recipeResultNode, recipeContext));
            }
            finally
            {
                logger.postStageComplete();
            }
        }
        finally
        {
            logger.done();
        }
    }

    private void publishEvent(Event evt)
    {
        buildHookManager.handleEvent(evt, logger);
        eventManager.publish(evt);
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
            if(recipeQueue.cancelRequest(recipeResult.getId()))
            {
                // We caught it now, so we are complete.
                complete();
            }
        }

    }

    public RecipeAssignmentRequest getAssignmentRequest()
    {
        return assignmentRequest;
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
        this.recipeQueue = queue;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setBuildHookManager(BuildHookManager buildHookManager)
    {
        this.buildHookManager = buildHookManager;
    }
}
