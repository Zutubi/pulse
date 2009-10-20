package com.zutubi.pulse.master;

import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.*;
import static com.zutubi.pulse.core.RecipeUtils.addResourceProperties;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.events.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.FeaturePersister;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
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
import static com.zutubi.pulse.master.scm.ScmClientUtils.ScmAction;
import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookManager;
import com.zutubi.pulse.servercore.CopyBootstrapper;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.util.logging.Logger;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public class RecipeController
{
    private static final Logger LOG = Logger.getLogger(RecipeController.class);

    private ProjectConfiguration projectConfiguration;
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
    /**
     * An explicit flag set on receipt of the recipe commenced event.  We don't
     * user {@link com.zutubi.pulse.core.model.RecipeResult#commenced()} as it
     * may be true without us ever having received the event (e.g. for a recipe
     * that errored out before commencing).
     */
    private boolean commencedEventReceived = false;
    private boolean finished = false;

    private RecipeQueue recipeQueue;
    private AgentService agentService;
    private EventManager eventManager;
    private MasterConfigurationManager configurationManager;
    private ResourceManager resourceManager;
    private BuildHookManager buildHookManager;
    private RecipeDispatchService recipeDispatchService;
    private ScmManager scmManager;

    public RecipeController(ProjectConfiguration projectConfiguration, BuildResult buildResult, RecipeResultNode recipeResultNode, RecipeAssignmentRequest assignmentRequest, PulseExecutionContext recipeContext, RecipeResultNode previousSuccessful, RecipeLogger logger, RecipeResultCollector collector)
    {
        this.projectConfiguration = projectConfiguration;
        this.buildResult = buildResult;
        this.recipeResultNode = recipeResultNode;
        this.recipeResult = recipeResultNode.getResult();
        this.assignmentRequest = assignmentRequest;
        this.recipeContext = recipeContext;
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
            addResourceProperties(recipeContext, assignmentRequest.getResourceRequirements(), resourceRepository);
        }

        // Update the request and its context before it is sent to the agent.
        RecipeRequest recipeRequest = assignmentRequest.getRequest();
        BuildRevision buildRevision = assignmentRequest.getRevision();
        recipeRequest.setPulseFileSource(buildRevision.getPulseFile());

        final ExecutionContext agentContext = recipeRequest.getContext();
        addRevisionProperties(agentContext, buildRevision);
        agentContext.addString(NAMESPACE_INTERNAL, BuildProperties.PROPERTY_AGENT, agent.getConfig().getName());
        agentContext.addValue(NAMESPACE_INTERNAL, BuildProperties.PROPERTY_AGENT_HANDLE, agent.getConfig().getHandle());
        agentContext.addString(NAMESPACE_INTERNAL, PROPERTY_CLEAN_BUILD, Boolean.toString(buildResult.getProject().isForceCleanForAgent(agent.getId())));

        addScmProperties(agentContext);

        // Now it may be dispatched.
        recipeDispatchService.dispatch(event);
    }

    private void addScmProperties(final ExecutionContext agentContext)
    {
        try
        {
            List<ResourceProperty> scmProperties = withScmClient(projectConfiguration.getScm(), scmManager, new ScmAction<List<ResourceProperty>>()
            {
                public List<ResourceProperty> process(ScmClient scmClient) throws ScmException
                {
                    return scmClient.getProperties(agentContext);
                }
            });

            for (ResourceProperty property: scmProperties)
            {
                agentContext.add(property);
            }
        }
        catch (ScmException e)
        {
            LOG.warning("Unable to add SCM properties: " + e.getMessage(), e);
        }
    }

    private void handleRecipeDispatched(RecipeDispatchedEvent event)
    {
        logger.log(event);
    }

    private void handleRecipeCommenced(RecipeCommencedEvent event)
    {
        commencedEventReceived = true;
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
            logger.close();
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

    public void terminateRecipe(String message)
    {
        recipeResult.terminate(message);
        buildManager.save(recipeResult);
        if (commencedEventReceived)
        {
            // Tell the agent service that it can stop trying to execute this
            // recipe.  We *must* have received the commenced event before we
            // can do this.
            agentService.terminateRecipe(recipeResult.getId());
        }
        else
        {
            // Not yet commenced, try and catch it at the recipe queue. If
            // we don't catch it, then we wait for the RecipeCommencedEvent.
            if (recipeQueue.cancelRequest(recipeResult.getId()))
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

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setRecipeDispatchService(RecipeDispatchService recipeDispatchService)
    {
        this.recipeDispatchService = recipeDispatchService;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
