/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.build.control;

import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.events.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.FeaturePersister;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.ResultCustomFields;
import com.zutubi.pulse.core.resources.api.AsResourcePropertyFunction;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.MasterBuildProperties;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.build.log.RecipeLogger;
import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.build.queue.RecipeQueue;
import com.zutubi.pulse.master.events.build.*;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.master.scheduling.CallbackService;
import com.zutubi.pulse.master.scm.MasterScmClientFactory;
import com.zutubi.pulse.master.tove.config.project.BuildOptionsConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookManager;
import com.zutubi.util.Constants;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

import static com.google.common.collect.Collections2.transform;
import static com.zutubi.pulse.core.RecipeUtils.addResourceProperties;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import static com.zutubi.pulse.master.MasterBuildProperties.addRevisionProperties;
import static com.zutubi.pulse.master.scm.ScmClientUtils.ScmAction;
import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;
import static com.zutubi.util.StringUtils.safeToString;

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
    private List<ResourceProperty> scmProperties;
    private BuildManager buildManager;
    /**
     * An explicit flag set on receipt of the recipe commenced event.  We don't
     * use {@link com.zutubi.pulse.core.model.RecipeResult#commenced()} as it
     * may be true without us ever having received the event (e.g. for a recipe
     * that errored out before commencing).
     */
    private boolean commencedEventReceived = false;
    private boolean finished = false;
    private RecipeTimeoutCallback timeoutCallback;

    private int retryCount;

    private RecipeQueue recipeQueue;
    private AgentService agentService;
    private EventManager eventManager;
    private MasterConfigurationManager configurationManager;
    private ResourceManager resourceManager;
    private BuildHookManager buildHookManager;
    private RecipeDispatchService recipeDispatchService;
    private MasterScmClientFactory scmClientFactory;
    private CallbackService callbackService;

    public RecipeController(ProjectConfiguration projectConfiguration, BuildResult buildResult, RecipeResultNode recipeResultNode, RecipeAssignmentRequest assignmentRequest, RecipeResultNode previousSuccessful, RecipeLogger logger, RecipeResultCollector collector, int retryCount)
    {
        this.projectConfiguration = projectConfiguration;
        this.buildResult = buildResult;
        this.recipeResultNode = recipeResultNode;
        this.retryCount = retryCount;
        this.recipeResult = recipeResultNode.getResult();
        this.assignmentRequest = assignmentRequest;
        this.previousSuccessful = previousSuccessful;
        this.logger = logger;
        this.collector = collector;
    }

    public void initialise(Bootstrapper bootstrapper)
    {
        try
        {
            initialiseContexts();

            collector.prepare(buildResult, recipeResultNode.getResult().getId());
            logger.prepare();
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

    private void initialiseContexts()
    {
        PulseExecutionContext agentContext = assignmentRequest.getRequest().getContext();
        recipeContext = new PulseExecutionContext(agentContext);

        BuildStageConfiguration stageConfig = projectConfiguration.getStage(recipeResultNode.getStageName());
        // The agent context does not include user-defined properties yet -- they are added once
        // the agent environment is available.
        MasterBuildProperties.addProjectProperties(agentContext, projectConfiguration, false);
        MasterBuildProperties.addProjectProperties(recipeContext, projectConfiguration, true);

        addValueToContexts(agentContext, recipeContext, PROPERTY_RECIPE_ID, Long.toString(recipeResult.getId()));
        addValueToContexts(agentContext, recipeContext, PROPERTY_RECIPE, stageConfig.getRecipe());
        addValueToContexts(agentContext, recipeContext, PROPERTY_STAGE, stageConfig.getName());
        addValueToContexts(agentContext, recipeContext, PROPERTY_STAGE_HANDLE, stageConfig.getHandle());
    }

    public int getRetryCount()
    {
        return retryCount;
    }

    public RecipeAssignmentRequest getAssignmentRequest()
    {
        return assignmentRequest;
    }

    public List<ResourceProperty> getScmProperties()
    {
        return scmProperties;
    }

    public boolean matchesRecipeEvent(RecipeEvent event)
    {
        return event.getRecipeId() == recipeResult.getId();
    }

    public void handleRecipeEvent(RecipeEvent event)
    {
        if (LOG.isLoggable(Level.FINER))
        {
            LOG.finer("Recipe controller (" + recipeResult.getId() + "): handle event: " + safeToString(event));
        }

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
        finally
        {
            if (LOG.isLoggable(Level.FINER))
            {
                LOG.finer("Recipe controller (" + recipeResult.getId() + "): event handled: " + safeToString(event));
            }
        }
    }

    private void handleRecipeAssigned(RecipeAssignedEvent event)
    {
        logger.log(event);
        Agent agent = event.getAgent();
        agentService = agent.getService();
        recipeResultNode.setAgentName(agent.getName());
        buildManager.save(recipeResultNode);

        // Update the request and its context before it is sent to the agent.
        RecipeRequest recipeRequest = assignmentRequest.getRequest();

        final ExecutionContext agentContext = recipeRequest.getContext();
        addRevisionProperties(agentContext, buildResult);
        addValueToContexts(agentContext, recipeContext, PROPERTY_BUILD_VERSION, buildResult.getVersion());
        addValueToContexts(agentContext, recipeContext, PROPERTY_AGENT, agent.getConfig().getName());
        addValueToContexts(agentContext, recipeContext, PROPERTY_AGENT_HANDLE, agent.getConfig().getHandle());
        addValueToContexts(agentContext, recipeContext, PROPERTY_AGENT_DATA_PATTERN, agent.getConfig().getStorage().getDataDirectory());
        addValueToContexts(agentContext, recipeContext, PROPERTY_HOST_ID, agent.getHost().getId());

        scmProperties = getScmProperties(agentContext);
        for (ResourceProperty property: scmProperties)
        {
            agentContext.add(property);
            recipeContext.add(property);
        }

        // update the context to be used for the post build actions with version details.
        addRevisionProperties(recipeContext, buildResult);
        recipeContext.addValue(NAMESPACE_INTERNAL, PROPERTY_BUILD_VERSION, buildResult.getVersion());

        Collection<ResourcePropertyConfiguration> agentProperties = agent.getConfig().getProperties().values();
        recipeRequest.addAllProperties(transform(agentProperties, new AsResourcePropertyFunction()));
        for (ResourcePropertyConfiguration propertyConfig: agentProperties)
        {
            ResourceProperty property = propertyConfig.asResourceProperty();
            recipeContext.add(property);
        }

        ResourceRepository resourceRepository = resourceManager.getAgentRepository(agent);
        if (resourceRepository != null)
        {
            addResourceProperties(recipeContext, assignmentRequest.getResourceRequirements(), resourceRepository);
        }

        MasterBuildProperties.addStageProperties(recipeContext, buildResult, recipeResultNode, configurationManager, false);
        sendPreStageEvent();

        // Now it may be dispatched.
        recipeDispatchService.dispatch(event);
    }

    public void addValueToContexts(ExecutionContext c1, ExecutionContext c2, String name, Object value)
    {
        c1.addValue(NAMESPACE_INTERNAL, name, value);
        c2.addValue(NAMESPACE_INTERNAL, name, value);
    }

    private List<ResourceProperty> getScmProperties(final ExecutionContext agentContext)
    {
        try
        {
            return withScmClient(projectConfiguration, projectConfiguration.getScm(), scmClientFactory, new ScmAction<List<ResourceProperty>>()
            {
                public List<ResourceProperty> process(ScmClient scmClient) throws ScmException
                {
                    return scmClient.getProperties(agentContext);
                }
            });
        }
        catch (ScmException e)
        {
            LOG.warning("Unable to add SCM properties: " + e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    private void handleRecipeDispatched(RecipeDispatchedEvent event)
    {
        logger.log(event);
    }

    private void handleRecipeCommenced(RecipeCommencedEvent event)
    {
        for (Map.Entry<String, String> pathProperty: event.getPathProperties().entrySet())
        {
            recipeContext.addString(NAMESPACE_INTERNAL, pathProperty.getKey(), pathProperty.getValue());
        }

        recipeContext.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE_TIMESTAMP, new SimpleDateFormat(TIMESTAMP_FORMAT_STRING).format(new Date(event.getStartTime())));
        recipeContext.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE_TIMESTAMP_MILLIS, Long.toString(event.getStartTime()));

        if (projectConfiguration.getOptions().getTimeout() != BuildOptionsConfiguration.TIMEOUT_NEVER)
        {
            scheduleTimeout();
        }

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
            sendTerminateRequest();
        }
        buildManager.save(recipeResult);
        logger.log(event, recipeResult);
    }

    private void scheduleTimeout()
    {
        Date timeoutAt = new Date(System.currentTimeMillis() + projectConfiguration.getOptions().getTimeout() * Constants.MINUTE);
        try
        {
            timeoutCallback = new RecipeTimeoutCallback(buildResult.getId(), recipeResult.getId());
            callbackService.registerCallback(timeoutCallback, timeoutAt);
        }
        catch (Exception e)
        {
            LOG.severe("Unable to schedule build timeout callback: " + e.getMessage(), e);
        }
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
        if (timeoutCallback != null)
        {
            try
            {
                callbackService.unregisterCallback(timeoutCallback);
            }
            catch (Exception ex)
            {
                LOG.warning("Unable to unregister timeout trigger: " + ex.getMessage(), ex);
            }
        }

        recipeResult.complete();
        recipeResult.abortUnfinishedCommands();

        MasterBuildProperties.addCompletedStageProperties(recipeContext, buildResult, recipeResultNode, configurationManager, false);

        buildManager.save(recipeResult);
        logger.complete(recipeResult);
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
            logger.collecting(recipeResult);
            collector.collect(buildResult, recipeResult.getId(), recipeContext, agentService);
            copyBuildScopedData();
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

    private void copyBuildScopedData()
    {
        File dataDir = configurationManager.getDataDirectory();
        File buildOutputDir = buildResult.getAbsoluteOutputDir(configurationManager.getDataDirectory());
        File recipeOutputDir = recipeResult.getAbsoluteOutputDir(dataDir);

        copyPulseFile(buildOutputDir, recipeOutputDir);
        copyBuildFields(buildOutputDir, recipeOutputDir);
    }

    private void copyPulseFile(File buildOutputDir, File recipeOutputDir)
    {
        File source = new File(recipeOutputDir, RecipeProcessor.PULSE_FILE);
        if (source.exists())
        {
            File dest = new File(buildOutputDir, RecipeProcessor.PULSE_FILE);

            // Only copy the first pulse file we see come back (they are all
            // identical).
            if (dest.exists())
            {
                FileSystemUtils.robustDelete(source);
            }
            else
            {
                try
                {
                    FileSystemUtils.robustRename(source, dest);
                }
                catch (IOException e)
                {
                    LOG.warning("Unable to rename pulse file from '" + source.getAbsolutePath() + "' to '" + dest.getAbsolutePath() + "'");
                }
            }
        }
    }

    private void copyBuildFields(File buildOutputDir, File recipeOutputDir)
    {
        File buildFieldsFile = new File(recipeOutputDir, RecipeProcessor.BUILD_FIELDS_FILE);
        if (buildFieldsFile.exists())
        {
            ResultCustomFields fromRecipeLoader = new ResultCustomFields(recipeOutputDir, RecipeProcessor.BUILD_FIELDS_FILE);
            Map<String,String> fromRecipeFields = fromRecipeLoader.load();

            ResultCustomFields toBuildLoader = new ResultCustomFields(buildOutputDir);
            Map<String, String> toBuildFields = toBuildLoader.load();
            toBuildFields.putAll(fromRecipeFields);
            toBuildLoader.store(toBuildFields);

            if (!buildFieldsFile.delete())
            {
                LOG.warning("Could not delete build fields file '" + buildFieldsFile.getAbsolutePath() + "'");
            }
        }
    }

    public void postStage()
    {
        try
        {
            logger.cleaning();
            collector.cleanup(recipeResult.getId(), recipeContext, agentService);
        }
        catch (Exception e)
        {
            LOG.warning("Unable to clean up recipe '" + recipeResult.getId() + "'", e);
        }
        finally
        {
            logger.cleaningComplete();
        }

        sendPostStageEvent();
    }

    public void sendPreStageEvent()
    {
        logger.preStage();
        try
        {
            publishEvent(new PreStageEvent(this, buildResult, recipeResultNode, recipeContext));
        }
        finally
        {
            logger.preStageComplete();
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
        handleRecipeTermination();
    }

    public void cancelRecipe(String message)
    {
        recipeResult.cancel(message);
        buildManager.save(recipeResult);
        handleRecipeTermination();
    }

    private void handleRecipeTermination()
    {
        if (commencedEventReceived)
        {
            // Tell the agent service that it can stop trying to execute this
            // recipe.  We *must* have received the commenced event before we
            // can do this.
            sendTerminateRequest();
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

    private void sendTerminateRequest()
    {
        agentService.terminateRecipe(recipeResult.getId());
        publishEvent(new TerminateStageEvent(this, buildResult, recipeResultNode, recipeContext));
    }

    public RecipeResult getResult()
    {
        return recipeResult;
    }

    public RecipeResultNode getResultNode()
    {
        return recipeResultNode;
    }
    
    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
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

    public void setScmClientFactory(MasterScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }

    public void setCallbackService(CallbackService callbackService)
    {
        this.callbackService = callbackService;
    }

    private class RecipeTimeoutCallback implements Runnable
    {
        private long buildResultId;
        private long recipeId;

        private RecipeTimeoutCallback(long buildResultId, long recipeId)
        {
            this.buildResultId = buildResultId;
            this.recipeId = recipeId;
        }

        public void run()
        {
            eventManager.publish(new RecipeTimeoutEvent(RecipeController.this, buildResultId, recipeId));
        }
    }}
