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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.zutubi.events.AsynchronousDelegatingListener;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.dependency.RepositoryAttributes;
import com.zutubi.pulse.core.dependency.ivy.IvyClient;
import com.zutubi.pulse.core.dependency.ivy.IvyConfiguration;
import com.zutubi.pulse.core.dependency.ivy.IvyManager;
import com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor;
import com.zutubi.pulse.core.engine.PulseFileProvider;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.events.RecipeCommencedEvent;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.core.events.RecipeEvent;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.AsResourcePropertyFunction;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.MasterBuildProperties;
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.build.log.BuildLogFile;
import com.zutubi.pulse.master.build.log.DefaultBuildLogger;
import com.zutubi.pulse.master.build.log.DefaultRecipeLogger;
import com.zutubi.pulse.master.build.log.RecipeLogFile;
import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.dependency.ivy.ModuleDescriptorFactory;
import com.zutubi.pulse.master.events.build.*;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.scm.MasterScmClientFactory;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.security.RepositoryAuthenticationProvider;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.project.*;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookManager;
import com.zutubi.pulse.servercore.PatchBootstrapper;
import com.zutubi.pulse.servercore.ProjectBootstrapper;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.events.FilteringListener;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.variables.ConfigurationVariableProvider;
import com.zutubi.tove.variables.api.VariableMap;
import com.zutubi.util.*;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.time.TimeStamps;
import org.apache.ivy.core.report.ResolveReport;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.google.common.collect.Collections2.transform;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import static com.zutubi.pulse.master.MasterBuildProperties.addRevisionProperties;
import static com.zutubi.pulse.master.scm.ScmClientUtils.*;
import static com.zutubi.util.StringUtils.safeToString;

/**
 * The DefaultBuildController is responsible for executing and coordinating a single
 * build request.
 */
public class DefaultBuildController implements EventListener, BuildController
{
    private static final Messages I18N = Messages.getInstance(DefaultBuildController.class);
    private static final Logger LOG = Logger.getLogger(DefaultBuildController.class);

    private BuildRequestEvent request;
    private Project project;
    private ProjectConfiguration projectConfig;

    private EventManager eventManager;
    private ProjectManager projectManager;
    private BuildManager buildManager;
    private ChangelistManager changelistManager;
    private DependencyManager dependencyManager;
    private TestManager testManager;
    private MasterLocationProvider masterLocationProvider;
    private MasterConfigurationManager configurationManager;
    private RecipeResultCollector collector;
    private List<RecipeController> controllers;
    private List<RecipeController> executingControllers = new LinkedList<RecipeController>();
    private BuildResult buildResult;
    private FilteringListener eventListener;
    private int pendingRecipes = 0;

    private File buildDir;
    private BuildResult previousHealthy;
    private PulseExecutionContext buildContext;
    private BuildHookManager buildHookManager;
    private RepositoryAuthenticationProvider repositoryAuthenticationProvider;

    private ScmManager scmManager;
    private MasterScmClientFactory scmClientFactory;
    private ThreadFactory threadFactory;
    private ObjectFactory objectFactory;

    private DefaultBuildLogger buildLogger;

    private IvyManager ivyManager;
    private IvyClient ivy;
    private IvyModuleDescriptor ivyModuleDescriptor;
    private RepositoryAttributes repositoryAttributes;
    private ModuleDescriptorFactory moduleDescriptorFactory;
    private ConfigurationVariableProvider configurationVariableProvider;
    private ConfigurationProvider configurationProvider;

    private boolean dependencyInfoRecorded = false;

    public DefaultBuildController(BuildRequestEvent event)
    {
        this.request = event;
        projectConfig = request.getProjectConfig();
    }

    public long start()
    {
        try
        {
            buildResult = request.createResult(projectManager, buildManager);
            // Fail early if things are not as expected.
            if (!buildResult.isPersistent())
            {
                throw new RuntimeException("Failed to persist build result.");
            }
        }
        catch (Exception e)
        {
            LOG.error(e);
            return 0;
        }

        // We filter events before they get to the async listeners queue so that builds are
        // independent of each other (if we didn't a stuck build could pile up a queue of events
        // from every other build, leaking resources).
        eventListener = new FilteringListener(new Predicate<Event>()
        {
            public boolean apply(Event event)
            {
                if (event instanceof BuildEvent)
                {
                    return ((BuildEvent) event).getBuildResult() == buildResult;
                }
                else if (event instanceof RecipeEvent)
                {
                    return ((RecipeEvent) event).getBuildId() == buildResult.getId();
                }
                else if (event instanceof BuildTerminationRequestEvent)
                {
                    return ((BuildTerminationRequestEvent)event).isTerminationRequested(buildResult.getId());
                }
                else
                {
                    return true;
                }
            }
        }, new AsynchronousDelegatingListener(this, "Controller for " + buildResult, threadFactory));

        // now that we have a persistent build result, all errors will be reported against
        // the result and a full cleanup is required.
        try
        {
            project = projectManager.getProject(projectConfig.getProjectId(), false);
            String repositoryUrl = configurationManager.getUserPaths().getRepositoryRoot().toURI().toString();
            ivy = ivyManager.createIvyClient(repositoryUrl, buildResult.getId());
            moduleDescriptorFactory = new ModuleDescriptorFactory(new IvyConfiguration(), configurationManager);
            previousHealthy = buildManager.getLatestBuildResult(project, true, ResultState.getHealthyStates());

            MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
            buildDir = paths.getBuildDir(buildResult);
            buildResult.setAbsoluteOutputDir(configurationManager.getDataDirectory(), buildDir);

            buildLogger = new DefaultBuildLogger(new BuildLogFile(buildResult, paths));
            ivy.pushMessageLogger(buildLogger.getMessageLogger());

            String authToken = activateBuildAuthenticationToken();
            buildContext = newContext(authToken);
            MasterBuildProperties.addProjectProperties(buildContext, projectConfig, true);
            for (ResourceProperty requestProperty : asResourceProperties(request.getProperties()))
            {
                buildContext.add(requestProperty);
            }

            controllers = new LinkedList<RecipeController>();
            createControllers(authToken);

            buildResult.queue();
            buildManager.save(buildResult);

            // We handle this event ourselves: this ensures that all processing of
            // the build from this point forth is handled by the single thread in
            // our async listener.  Basically, given events could be coming from
            // anywhere, even for different builds, it is much safer to ensure we
            // *only* use that thread after we have registered the listener.
            eventManager.register(eventListener);

            // handle the event directly, there is no need to expose this event to the wider audience.
            eventListener.handleEvent(new BuildControllerBootstrapEvent(this, buildResult, buildContext));
        }
        catch (Exception e)
        {
            LOG.warning("Could not start build controller for '" + buildResult + "':" + e.getMessage(), e);

            // handle the event directly, there is no need to expose this event to the wider audience.
            eventListener.handleEvent(new BuildControllerBootstrapEvent(this, buildResult, buildContext, e));
        }
        return buildResult.getNumber();
    }

    private PulseExecutionContext newContext(String authToken)
    {
        // Creates a context with everything that should be shared by the build and recipe
        // contexts.  Note this excludes resource properties as they need to be added later for
        // recipes (when we are on the agent) and we want to avoid adding them twice (CIB-3090).
        PulseExecutionContext context = new PulseExecutionContext();
        MasterBuildProperties.addBuildProperties(context, buildResult, project, buildDir, masterLocationProvider.getMasterUrl(), configurationProvider.get(GlobalConfiguration.class).getBaseUrl());
        context.addString(NAMESPACE_INTERNAL, PROPERTY_RETRIEVAL_PATTERN, projectConfig.getDependencies().getRetrievalPattern());
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_UNZIP_RETRIEVED_ARCHIVES, projectConfig.getDependencies().isUnzipRetrievedArchives());
        context.addString(NAMESPACE_INTERNAL, PROPERTY_SYNC_DESTINATION, Boolean.toString(projectConfig.getDependencies().isSyncDestination()));
        // We resolve the SCM properties on the master as the full context is not available on
        // the agents.
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_SCM_CONFIGURATION, configurationVariableProvider.resolveStringProperties(projectConfig.getScm(), configurationVariableProvider.variablesForConfiguration(projectConfig)));
        context.setSecurityHash(authToken);
        return context;
    }

    /**
     * To each build context we add a token that can later be used by any of the builds processes to
     * access the internal pulse artifact repository.  This token will be valid for the duration of the
     * build.
     */
    private String activateBuildAuthenticationToken()
    {
        String token = RandomUtils.secureRandomString(15);
        repositoryAuthenticationProvider.activate(token);
        return token;
    }

    /**
     * Deactivate / invalidate the authentication token in the current build context.  This must
     * be done at the end of the build.
     */
    private void deactivateBuildAuthenticationToken()
    {
        String token = buildContext.getSecurityHash();
        repositoryAuthenticationProvider.deactivate(token);
    }

    private void createControllers(String authToken)
    {
        PulseFileProvider pulseFileProvider = getPulseFileSource();

        for (BuildStageConfiguration stageConfig : projectConfig.getStages().values())
        {
            RecipeResultNode stageResult = createResultForStage(stageConfig);
            if (stageConfig.isEnabled())
            {
                createControllerForStage(stageConfig, stageResult, pulseFileProvider, authToken);
            }
            else
            {
                RecipeResult recipeResult = stageResult.getResult();
                recipeResult.skip();
                recipeResult.complete();
                buildManager.save(recipeResult);
            }
        }
    }

    private RecipeResultNode createResultForStage(BuildStageConfiguration stageConfig)
    {
        RecipeResult recipeResult = new RecipeResult(stageConfig.getRecipe());
        RecipeResultNode stageResult = new RecipeResultNode(stageConfig, recipeResult);
        buildResult.addStage(stageResult);
        buildManager.save(buildResult);
        return stageResult;
    }

    private RecipeController createControllerForStage(BuildStageConfiguration stageConfig, RecipeResultNode stageResult, PulseFileProvider pulseFileProvider, String authToken)
    {
        RecipeResult recipeResult = stageResult.getResult();
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File recipeOutputDir = paths.getOutputDir(buildResult, recipeResult.getId());
        recipeResult.setAbsoluteOutputDir(configurationManager.getDataDirectory(), recipeOutputDir);

        RecipeRequest recipeRequest = new RecipeRequest(newContext(authToken));
        recipeRequest.setPulseFileSource(pulseFileProvider);
        recipeRequest.addAllProperties(asResourceProperties(projectConfig.getProperties().values()));
        recipeRequest.addAllProperties(asResourceProperties(stageConfig.getProperties().values()));
        recipeRequest.addAllProperties(asResourceProperties(request.getProperties()));
        List<ResourceRequirement> resourceRequirements = getResourceRequirements(stageConfig, recipeRequest);
        recipeRequest.addAllResourceRequirements(resourceRequirements);

        RecipeAssignmentRequest assignmentRequest = new RecipeAssignmentRequest(project, getAgentRequirements(stageConfig), resourceRequirements, recipeRequest, buildResult);
        setRequestPriority(assignmentRequest, stageConfig);

        RecipeResultNode previousRecipe = previousHealthy == null ? null : previousHealthy.findResultNodeByHandle(stageConfig.getHandle());
        DefaultRecipeLogger logger = new DefaultRecipeLogger(new RecipeLogFile(buildResult, recipeResult.getId(), paths), projectConfig.getOptions().isLiveLogsEnabled());
        RecipeController recipeController = objectFactory.buildBean(RecipeController.class, projectConfig, buildResult, stageResult, assignmentRequest, previousRecipe, logger, collector, 0);
        controllers.add(recipeController);
        pendingRecipes++;

        return recipeController;
    }

    private void retry(RecipeController controller, RecipeErrorEvent errorEvent)
    {
        RecipeResultNode stageResult = controller.getResultNode();
        BuildStageConfiguration stageConfig = projectConfig.getStage(stageResult.getStageName());
        String message = "Retrying stage '" + stageResult.getStageName() + "' due to problem on agent '" + stageResult.getAgentNameSafe() + "': " + errorEvent.getErrorMessage();
        buildLogger.status(message);
        buildResult.addFeature(Feature.Level.INFO, message);

        deleteStage(stageResult);

        stageResult = createResultForStage(stageConfig);
        RecipeController newController = createControllerForStage(stageConfig, stageResult, getPulseFileSource(), controller.getAssignmentRequest().getRequest().getContext().getSecurityHash());
        executingControllers.add(newController);
        newController.initialise(controller.getAssignmentRequest().getRequest().getBootstrapper());
        checkControllerStatus(newController, null);
    }

    private void deleteStage(RecipeResultNode stageResult)
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File recipeOutputDir = paths.getOutputDir(buildResult, stageResult.getResult().getId());
        try
        {
            FileSystemUtils.rmdir(recipeOutputDir);
        }
        catch (IOException e)
        {
            LOG.warning(e);
        }

        buildResult.removeStage(stageResult);
        buildManager.save(buildResult);
    }

    private PulseFileProvider getPulseFileSource()
    {
        try
        {
            return projectConfig.getType().getPulseFile();
        }
        catch (Exception e)
        {
            throw new BuildException("Unable to retrieve pulse file: " + e.getMessage(), e);
        }
    }

    private void setRequestPriority(RecipeAssignmentRequest assignmentRequest, BuildStageConfiguration stageConfig)
    {
        if (request.getOptions().hasPriority())
        {
            assignmentRequest.setPriority(request.getOptions().getPriority());
        }
        else if (stageConfig.hasPriority())
        {
            assignmentRequest.setPriority(stageConfig.getPriority());
        }
        else if (request.getProjectConfig().getOptions().hasPriority())
        {
            assignmentRequest.setPriority(request.getProjectConfig().getOptions().getPriority());
        }
    }

    private Collection<? extends ResourceProperty> asResourceProperties(Collection<ResourcePropertyConfiguration> resourcePropertyConfigurations)
    {
        return transform(resourcePropertyConfigurations, new AsResourcePropertyFunction());
    }

    private AgentRequirements getAgentRequirements(BuildStageConfiguration stage)
    {
        AgentRequirements stageRequirements = stage.getAgentRequirements();
        if (buildResult.isPersonal())
        {
            stageRequirements = new PersonalBuildAgentRequirements(stageRequirements);
        }
        return stageRequirements;
    }

    private List<ResourceRequirement> getResourceRequirements(BuildStageConfiguration node, RecipeRequest recipeRequest)
    {
        // get the list of resource requirements for the project AND the particular stage we are running.
        List<ResourceRequirement> requirements = new LinkedList<ResourceRequirement>();
        PulseScope requirementsScope = new PulseScope(recipeRequest.getContext().getScope());
        for(ResourceProperty property: recipeRequest.getProperties())
        {
            requirementsScope.add(property);
        }
        
        requirements.addAll(asResourceRequirements(projectConfig.getRequirements(), requirementsScope));
        requirements.addAll(asResourceRequirements(node.getRequirements(), requirementsScope));

        // If the SCM has an implicit resource not conflicting with those configured, add it too.
        try
        {
            final String implicitResource = withScmClient(projectConfig, projectConfig.getScm(), scmClientFactory, new ScmAction<String>()
            {
                public String process(ScmClient scmClient) throws ScmException
                {
                    return scmClient.getImplicitResource();
                }
            });

            if (StringUtils.stringSet(implicitResource))
            {
                if (!Iterables.any(requirements, new Predicate<ResourceRequirement>()
                {
                    public boolean apply(ResourceRequirement resourceRequirement)
                    {
                        return resourceRequirement.getResource().equals(implicitResource);
                    }
                }))
                {
                    requirements.add(new ResourceRequirement(implicitResource, false, true));
                }
            }
        }
        catch (ScmException e)
        {
            LOG.warning("Unable to get implicit SCM resource: " + e.getMessage(), e);
        }

        return requirements;
    }

    private Collection<? extends ResourceRequirement> asResourceRequirements(List<ResourceRequirementConfiguration> requirements, final VariableMap variables)
    {
        return transform(requirements, new Function<ResourceRequirementConfiguration, ResourceRequirement>()
        {
            public ResourceRequirement apply(ResourceRequirementConfiguration config)
            {
                return config.asResourceRequirement(variables);
            }
        });
    }

    public void handleEvent(Event evt)
    {
        if (LOG.isLoggable(Level.FINER))
        {
            LOG.finer("Build controller (" + buildResult.getId() + "): handle event: " + safeToString(evt));
        }

        try
        {
            if (evt instanceof BuildControllerBootstrapEvent)
            {
                BuildControllerBootstrapEvent e = (BuildControllerBootstrapEvent) evt;
                if (e.getBuildResult() == buildResult)
                {
                    handleControllerBootstrap(e);
                }
            }
            else if (evt instanceof BuildStatusEvent)
            {
                BuildStatusEvent e = (BuildStatusEvent) evt;
                if (e.getBuildResult() == buildResult)
                {
                    buildLogger.status(e.getMessage());
                }
            }
            else if (evt instanceof BuildTerminationRequestEvent)
            {
                handleBuildTerminationRequest((BuildTerminationRequestEvent) evt);
            }
            else if (evt instanceof RecipeTimeoutEvent)
            {
                handleRecipeTimeout((RecipeTimeoutEvent) evt);
            }
            else if (evt instanceof RecipeEvent)
            {
                handleRecipeEvent((RecipeEvent) evt);
            }
            else
            {
                LOG.warning("Build controller received unexpected event of type " + evt.getClass().getName());
            }
        }
        catch (BuildException e)
        {
            buildResult.error(e);
            completeBuild(true);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            buildResult.error();
            recordUnexpectedError(Feature.Level.ERROR, e, "Handling " + evt.getClass().getSimpleName());
            completeBuild(true);
        }
        finally
        {
            if (LOG.isLoggable(Level.FINER))
            {
                LOG.finer("Build controller (" + buildResult.getId() + "): event handled: " + safeToString(evt));
            }
        }
    }

    private void handleControllerBootstrap(BuildControllerBootstrapEvent e)
    {
        // It is important that this directory is created *after* the build
        // result is commenced and saved to the database, so that the
        // database knows of the possibility of some other persistent
        // artifacts, even if an error occurs very early in the build.
        File buildDir = buildResult.getAbsoluteOutputDir(configurationManager.getDataDirectory());
        if (!buildDir.mkdirs())
        {
            throw new BuildException("Unable to create build directory '" + buildDir.getAbsolutePath() + "'");
        }

        if (!buildManager.isSpaceAvailableForBuild())
        {
            throw new BuildException("Insufficient database space to run build.  Consider adding more cleanup rules to remove old build information");
        }

        // delay propagating this startup exception until after the build directory
        // is available to record the exception.
        if (e.hasStartupException())
        {
            throw new BuildException(e.getStartupException());
        }

        buildLogger.prepare();
        buildLogger.preamble(buildResult);
        buildLogger.preBuild();
        publishEvent(new PreBuildEvent(this, buildResult, buildContext));
        buildLogger.preBuildCompleted();

        if (controllers.size() > 0)
        {
            initialiseControllers();
        }
        else
        {
            // If there are no controllers, then there is nothing more to be done.
            // Complete the build now as we will not be receiving event triggered callbacks
            // to complete the build.
            handleBuildCommenced();
            completeBuild(false);
        }
    }

    private Bootstrapper createPersonalBuildBootstrapper(final Bootstrapper initialBootstrapper)
    {
        PersonalBuildRequestEvent pbr = ((PersonalBuildRequestEvent) request);
        return new PatchBootstrapper(initialBootstrapper, pbr.getUser().getId(), pbr.getNumber(), pbr.getPatchFormat());
    }

    private void handleBuildTerminationRequest(BuildTerminationRequestEvent event)
    {
        if (event.isTerminationRequested(buildResult.getId()))
        {
            terminateBuild(event.getMessage(), I18N.format("terminate.stage.request"), event.isKill());
        }
    }

    private void cancelBuild(String buildMessage, final String recipeMessage)
    {
        buildResult.cancel(buildMessage);
        buildManager.save(buildResult);

        handleControllerTermination(new UnaryProcedure<RecipeController>()
        {
            public void run(RecipeController recipeController)
            {
                recipeController.cancelRecipe(recipeMessage);
            }
        });
    }

    private void terminateBuild(String buildMessage, final String recipeMessage, boolean kill)
    {
        // Mark the build terminating (so it will go into the terminated state
        // on completion).
        buildResult.terminate(buildMessage);
        buildManager.save(buildResult);

        if (kill)
        {
            completeBuild(true);
        }
        else
        {
            // Allow the recipe controllers to terminate gracefully.
            handleControllerTermination(new UnaryProcedure<RecipeController>()
            {
                public void run(RecipeController recipeController)
                {
                    recipeController.terminateRecipe(recipeMessage);
                }
            });
        }
    }

    private void handleControllerTermination(UnaryProcedure<RecipeController> terminationProcedure)
    {
        List<RecipeController> terminatedNodes = new ArrayList<RecipeController>(executingControllers.size());

        if (executingControllers.size() > 0)
        {
            for (RecipeController controller: executingControllers)
            {
                terminationProcedure.run(controller);

                if (handleIfFinished(controller, false))
                {
                    terminatedNodes.add(controller);
                }
            }

            executingControllers.removeAll(terminatedNodes);
        }

        if (executingControllers.size() == 0)
        {
            completeBuild(false);
        }
    }

    private void handleRecipeTimeout(RecipeTimeoutEvent event)
    {
        LOG.debug("Recipe timeout event received for build " + event.getBuildId() + ", recipe " + event.getRecipeId());
        RecipeController found = null;
        for (RecipeController controller: executingControllers)
        {
            if (controller.getResult().getId() == event.getRecipeId())
            {
                found = controller;
                break;
            }
        }

        if (found != null)
        {
            LOG.debug("Terminating recipe for build " + event.getBuildId() + ", recipe " + event.getRecipeId());
            found.terminateRecipe("Timed out");
            if (handleIfFinished(found, false))
            {
                executingControllers.remove(found);
                if (executingControllers.size() == 0)
                {
                    completeBuild(false);
                }
                else
                {
                    checkForTermination(found);
                }
            }
        }
    }

    private void initialiseControllers()
    {
        Bootstrapper bootstrapper = new ProjectBootstrapper(projectConfig.getName());
        if (request.isPersonal())
        {
            bootstrapper = createPersonalBuildBootstrapper(bootstrapper);
        }

        // Important to add them all first as a failure during initialisation
        // will test if there are other executing controllers (if not the
        // build is finished).
        executingControllers.addAll(controllers);

        List<RecipeController> sortedControllers = new LinkedList<RecipeController>(controllers);
        Collections.sort(sortedControllers, new AssignmentRequestPriorityComparator());

        for (RecipeController controller : sortedControllers)
        {
            controller.initialise(bootstrapper);
            checkControllerStatus(controller, null);
        }
    }

    private void handleRecipeEvent(RecipeEvent e)
    {
        if (e instanceof RecipeCollectingEvent || e instanceof RecipeCollectedEvent)
        {
            // Ignore these.
            return;
        }

        RecipeController controller = null;
        for (RecipeController c: executingControllers)
        {
            if (c.matchesRecipeEvent(e))
            {
                controller = c;
                break;
            }
        }

        if (controller != null)
        {
            RecipeErrorEvent errorEvent = null;

            // If we got here we are sure that the event was for one of our
            // recipes.
            if (e instanceof RecipeCommencedEvent)
            {
                pendingRecipes--;

                if (pendingRecipes == 0)
                {
                    handleLastCommenced();
                }

            }
            else if (e instanceof RecipeAssignedEvent)
            {
                if (!buildResult.commenced())
                {
                    handleBuildCommenced();
                }
            }
            else if (e instanceof RecipeErrorEvent)
            {
                errorEvent = (RecipeErrorEvent) e;
            }

            controller.handleRecipeEvent(e);
            checkControllerStatus(controller, errorEvent);
        }
    }

    private void handleLastCommenced()
    {
        // We can now make a more accurate estimate of our remaining running
        // time, as there are no more queued recipes.
        long longestRemaining = getMaximumEstimatedTimeRemaining();
        TimeStamps buildStamps = buildResult.getStamps();
        long estimatedEnd = System.currentTimeMillis() + longestRemaining;
        if (estimatedEnd > buildStamps.getStartTime())
        {
            buildStamps.setEstimatedRunningTime(estimatedEnd - buildStamps.getStartTime());
        }
    }

    public long getMaximumEstimatedTimeRemaining()
    {
        long longestRemaining = 0;
        for (RecipeController controller : controllers)
        {
            TimeStamps stamps = controller.getResult().getStamps();
            if (stamps.hasEstimatedEndTime())
            {
                long remaining = stamps.getEstimatedTimeRemaining();
                if (remaining > longestRemaining)
                {
                    longestRemaining = remaining;
                }
            }
        }
        return longestRemaining;
    }

    /**
     * Called when the first recipe for this build is assigned.  It is at
     * this point that the build is said to have commenced.
     */
    private void handleBuildCommenced()
    {
        publishEvent(new BuildCommencingEvent(this, buildResult, buildContext));

        BuildRevision buildRevision = request.getRevision();
        if (!buildRevision.isInitialised())
        {
            buildLogger.status("Initialising build revision...");
            buildRevision.initialiseRevision();
            buildLogger.status("Revision initialised to '" + buildRevision.getRevision().getRevisionString() + "'");
        }

        getChanges(buildRevision);
        buildResult.commence();
        buildLogger.commenced(buildResult);

        addRevisionProperties(buildContext, buildResult);

        resolveDependencies();

        buildContext.addValue(NAMESPACE_INTERNAL, PROPERTY_BUILD_VERSION, buildResult.getVersion());

        if (previousHealthy != null)
        {
            buildResult.getStamps().setEstimatedRunningTime(previousHealthy.getStamps().getElapsed());
        }
        buildManager.save(buildResult);
    }

    private void resolveDependencies()
    {
        String version = request.getVersion();
        if (request.getOptions().isResolveVersion())
        {
            version = buildContext.resolveVariables(version);
        }
        buildResult.setVersion(version);

        ivyModuleDescriptor = moduleDescriptorFactory.createRetrieveDescriptor(projectConfig, buildResult, version);
        ivyModuleDescriptor.setStatus(buildResult.getStatus());

        if (ivyModuleDescriptor.getDescriptor().getConfigurations().length > 0)
        {
            buildLogger.preIvyResolve();
            ResolveReport resolveReport;
            try
            {
                // Resolves the descriptor to the cache.
                resolveReport = ivy.resolveDescriptor(ivyModuleDescriptor);
                @SuppressWarnings("unchecked")
                List<String> problemMessages = resolveReport.getAllProblemMessages();
                if (problemMessages == null)
                {
                    problemMessages = Collections.emptyList();
                }

                // Delivery actually generates the resolved descriptor.
                ivyModuleDescriptor = ivy.deliverDescriptor(ivyModuleDescriptor);
                buildLogger.postIvyResolve(problemMessages.toArray(new String[problemMessages.size()]));
            }
            catch (Exception e)
            {
                throw new BuildException("Unable to resolve dependencies: " + e.getMessage(), e);
            }
        }

        for (RecipeController recipeController: controllers)
        {
            recipeController.getAssignmentRequest().getRequest().getContext().addValue(NAMESPACE_INTERNAL, PROPERTY_DEPENDENCY_DESCRIPTOR, ivyModuleDescriptor.getDescriptor());
        }
    }

    private void getChanges(BuildRevision buildRevision)
    {
        Revision revision = buildRevision.getRevision();
        buildResult.setRevision(revision);

        if (!buildResult.isPersonal() && !buildResult.isUserRevision())
        {
            ScmConfiguration scm = projectConfig.getScm();
            Revision previousRevision = buildManager.getPreviousRevision(project);

            if (previousRevision != null)
            {
                ScmClient client = null;
                try
                {
                    buildLogger.status("Retrieving changes in build...");
                    Set<ScmCapability> capabilities = getCapabilities(project.getConfig(), project.getState(), scmManager);
                    if (capabilities.contains(ScmCapability.CHANGESETS))
                    {
                        client = scmManager.createClient(projectConfig, scm);
                        ScmContext context = scmManager.createContext(projectConfig, project.getState(), client.getImplicitResource());

                        List<Changelist> scmChanges = client.getChanges(context, previousRevision, revision);

                        for (Changelist changelist : scmChanges)
                        {
                            PersistentChangelist persistentChangelist = new PersistentChangelist(changelist);
                            persistentChangelist.setProjectId(buildResult.getProject().getId());
                            persistentChangelist.setResultId(buildResult.getId());
                            changelistManager.save(persistentChangelist);
                        }
                    }
                }
                catch (ScmException e)
                {
                    LOG.warning("Unable to retrieve changelist details from SCM server: " + e.getMessage(), e);
                }
                catch (Exception e)
                {
                    LOG.warning("Unexpected error retrieving changelist details from SCM server: " + e.getMessage(), e);
                    recordUnexpectedError(Feature.Level.WARNING, e, "Retrieving changelist details from SCM server");
                }
                finally
                {
                    buildLogger.status("Changes retrieved.");
                    IOUtils.close(client);
                }
            }
        }
    }

    private void recordUnexpectedError(Feature.Level level, Exception exception, String context)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println("Unexpected error: " + context + ":");
        exception.printStackTrace(printWriter);
        buildResult.addFeature(level, stringWriter.toString());
    }

    private boolean handleIfFinished(RecipeController controller, boolean collect)
    {
        if (controller.isFinished())
        {
            // the start of the post recipe execution handling.
            publishEvent(new RecipeCollectingEvent(this, buildResult.getId(), controller.getResult().getId()));

            if (collect)
            {
                controller.collect(buildResult);
            }

            controller.postStage();

            // the end of the post recipe execution handling.
            publishEvent(new RecipeCollectedEvent(this, buildResult.getId(), controller.getResult().getId()));
            return true;
        }

        return false;
    }

    private void checkControllerStatus(RecipeController controller, RecipeErrorEvent errorEvent)
    {
        if (handleIfFinished(controller, true))
        {
            executingControllers.remove(controller);
            if (errorEvent != null && errorEvent.isAgentStatusProblem() &&
                controller.getRetryCount() < projectConfig.getOptions().getStageRetriesOnAgentProblem())
            {
                retry(controller, errorEvent);
            }
            else
            {
                RecipeResult result = controller.getResult();
                if (result.failed())
                {
                    buildResult.addFeature(Feature.Level.ERROR, "Recipe " + result.getRecipeNameSafe() + " failed");
                }
                else if (result.errored())
                {
                    buildResult.addFeature(Feature.Level.ERROR, "Error executing recipe " + result.getRecipeNameSafe());
                }

                buildManager.save(buildResult);
            }

            if (executingControllers.size() == 0)
            {
                completeBuild(false);
            }
            else
            {
                checkForTermination(controller);
            }
        }
    }

    /**
     * Trigger build cancellation if the controllers stage has not succeeded and
     * is marked to terminate build.
     *
     * @param controller the stage controller
     */
    private void checkForTermination(RecipeController controller)
    {
        RecipeResultNode recipeResultNode = controller.getResultNode();
        if (!recipeResultNode.getResult().healthy())
        {
            String stageName = recipeResultNode.getStageName();
            if (projectConfig.getStage(stageName).isTerminateBuildOnFailure())
            {
                cancelBuild(I18N.format("terminate.stage.failure", stageName),
                        I18N.format("cancelled.stage.failure", stageName)
                );
            }
            else if (stageFailureLimitReached())
            {
                int stageFailureLimit = projectConfig.getOptions().getStageFailureLimit();
                cancelBuild(I18N.format("terminate.multiple.failures", stageFailureLimit),
                        I18N.format("cancelled.multiple.failures", stageFailureLimit)
                );
            }
        }
    }

    private boolean stageFailureLimitReached()
    {
        int limit = projectConfig.getOptions().getStageFailureLimit();
        if (limit == 0)
        {
            return false;
        }
        else
        {
            final int[] failures = new int[]{0};
            buildResult.forEachNode(new UnaryProcedure<RecipeResultNode>()
            {
                public void run(RecipeResultNode recipeResultNode)
                {
                    RecipeResult result = recipeResultNode.getResult();
                    if (result != null && result.completed() && !result.healthy())
                    {
                        failures[0]++;
                    }
                }
            });

            return limit == failures[0];
        }
    }

    private void completeBuild(boolean hard)
    {
        if (!hard && !dependencyInfoRecorded)
        {
            // These steps need to be taken before the build is marked as complete, so anything awaiting that moment can
            // be sure the repository and dependency links are up to date.  We'll also only attempt this once.
            dependencyInfoRecorded = true;
            recordDependencyInformation();
        }

        // Calculate the feature counts at the end of the build so that the result hierarchy does not need to be
        // traversed when this information is required.  This info may be queried via the remote API, so set it before
        // marking the build complete.
        buildResult.calculateFeatureCounts();

        // Tries extra hard to ensure a completed build is saved.  If it can't, this controller
        // must stay alive to handle later attempts.
        if (!failsafeComplete())
        {
            return;
        }

        // Now we are sure the build has been saved in a complete state.  We have more to do, and
        // although it's not ideal if something here fails unexpectedly, we can allow this
        // controller to complete.  We just must make sure the completed event goes out to allow
        // scheduling of further builds.
        try
        {
            long start = System.currentTimeMillis();
            testManager.index(buildResult);
            long duration = System.currentTimeMillis() - start;
            if (duration > 300000)
            {
                LOG.warning("Test case indexing for project %s took %f seconds", projectConfig.getName(), duration / 1000.0);
            }

            if (!hard)
            {
                // The timing of this event is important: handlers of this event
                // are allowed to add information to and modify the state of the
                // build result.  Hence it is crucial that indexing and a final
                // save are done afterwards.
                MasterBuildProperties.addCompletedBuildProperties(buildContext, buildResult, configurationManager);
                addCommonScmProperties();
                buildLogger.postBuild();
                publishEvent(new PostBuildEvent(this, buildResult, buildContext));
                buildLogger.postBuildCompleted();
            }

            // Another save is required as hooks may change the build.
            buildManager.save(buildResult);

            if (ivy != null)
            {
                ivy.cleanup();
            }
            
            deactivateBuildAuthenticationToken();
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }

        eventManager.unregister(eventListener);
        publishEvent(new BuildCompletedEvent(this, buildResult, buildContext));

        buildLogger.close();

        // this must be last since we are in fact stopping the thread running this method, we are
        // after all responding to an event on this listener.
        AsynchronousDelegatingListener asyncListener = (AsynchronousDelegatingListener) eventListener.getDelegate();
        asyncListener.stop(true);
    }

    private void recordDependencyInformation()
    {
        try
        {
            if (buildResult.getWorstStageState().isHealthy() && !buildResult.isPersonal())
            {
                try
                {
                    // publish this builds ivy file to the repository, making its artifacts available
                    // to subsequent builds.
                    publishIvyToRepository();
                }
                catch (BuildException e)
                {
                    buildResult.error(e);
                }
            }

            // Make these DB updates before the post-build event goes out, in case they influence things that hang off
            // that event (such as hooks).
            if (ivyModuleDescriptor != null)
            {
                dependencyManager.addDependencyLinks(buildResult, ivyModuleDescriptor);
            }
        }
        catch (Exception e)
        {
            // We don't want to prevent build completion for this, as ugly as it may be.
            buildResult.error(e.getMessage());
            LOG.severe(e);
        }
    }

    private boolean failsafeComplete()
    {
        RetryHandler retryHandler = new RetryHandler(5, TimeUnit.SECONDS, 300, TimeUnit.SECONDS);
        try
        {
            while (true)
            {
                try
                {
                    abortUnfinishedRecipes();
                    buildResult.complete();
                    if (buildLogger != null)
                    {
                        buildLogger.completed(buildResult);
                    }
                    buildManager.save(buildResult);
                    return true;
                }
                catch (Throwable t)
                {
                    retryHandler.handle(t);
                }
            }
        }
        catch (RetriesExhaustedException e)
        {
            LOG.severe("Unable to save completed build result, check your database connection then retry by cancelling the build: " + e.getMessage(), e);
            return false;
        }
    }

    private void addCommonScmProperties()
    {
        Iterable<RecipeController> filteredControllers = Iterables.filter(controllers, new Predicate<RecipeController>()
        {
            public boolean apply(RecipeController recipeController)
            {
                return recipeController.getScmProperties() != null;
            }
        });

        Iterator<RecipeController> recipeIt = filteredControllers.iterator();
        if (!recipeIt.hasNext())
        {
            return;
        }

        final List<ResourceProperty> commonProperties = new LinkedList<ResourceProperty>(recipeIt.next().getScmProperties());
        while (recipeIt.hasNext())
        {
            final List<ResourceProperty> properties = recipeIt.next().getScmProperties();
            Iterables.removeIf(commonProperties, Predicates.not(new ContainsMatchingPropertyPredicate(properties)));
        }

        for (ResourceProperty property: commonProperties)
        {
            buildContext.add(property);
        }
    }

    /**
     * Publish an ivy file to the repository.
     */
    private void publishIvyToRepository()
    {
        buildLogger.preIvyPublish();
        try
        {
            ivyModuleDescriptor.setBuildNumber(buildResult.getNumber());
            moduleDescriptorFactory.addArtifacts(buildResult, ivyModuleDescriptor);

            long projectHandle = buildContext.getLong(PROPERTY_PROJECT_HANDLE, 0);
            if (projectHandle != 0)
            {
                String path = ivyModuleDescriptor.getPath();
                repositoryAttributes.addAttribute(PathUtils.getParentPath(path), RepositoryAttributes.PROJECT_HANDLE, String.valueOf(projectHandle));
            }

            ivy.publishArtifacts(ivyModuleDescriptor);
            ivy.publishDescriptor(ivyModuleDescriptor);
            buildLogger.postIvyPublish();
        }
        catch (UnknownHostException e)
        {
            buildLogger.postIvyPublish("Unknown host: " + e.getMessage());
            throw new BuildException("Failed to publish the build artifacts to the repository because Pulse could not " +
                    "contact host \""+e.getMessage()+"\". Please check the Administration | settings | master host " +
                    "configuration is correct and the host accessible.", e);
        }
        catch (Exception e)
        {
            buildLogger.postIvyPublish(e.getClass().getName() + ": " + e.getMessage());
            throw new BuildException("Failed to publish the build artifacts to the repository. " +
                    e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    private void abortUnfinishedRecipes()
    {
        buildResult.abortUnfinishedRecipes();
        for (RecipeController controller : executingControllers)
        {
            eventManager.publish(new RecipeAbortedEvent(this, buildResult.getId(), controller.getResult().getId()));
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildStatusEvent.class, RecipeEvent.class, BuildTerminationRequestEvent.class };
    }

    public Project getProject()
    {
        return project;
    }

    public long getBuildResultId()
    {
        if (buildResult != null)
        {
            return buildResult.getId();
        }
        return -1;
    }

    private void publishEvent(Event evt)
    {
        if (evt instanceof BuildEvent)
        {
            buildHookManager.handleEvent(evt, buildLogger);
        }

        eventManager.publish(evt);
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setChangelistManager(ChangelistManager changelistManager)
    {
        this.changelistManager = changelistManager;
    }

    public void setDependencyManager(DependencyManager dependencyManager)
    {
        this.dependencyManager = dependencyManager;
    }

    public void setTestManager(TestManager testManager)
    {
        this.testManager = testManager;
    }

    public void setCollector(RecipeResultCollector collector)
    {
        this.collector = collector;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }

    public void setBuildHookManager(BuildHookManager buildHookManager)
    {
        this.buildHookManager = buildHookManager;
    }

    public void setRepositoryAuthenticationProvider(RepositoryAuthenticationProvider repositoryAuthenticationProvider)
    {
        this.repositoryAuthenticationProvider = repositoryAuthenticationProvider;
    }

    public void setIvyManager(IvyManager ivyManager)
    {
        this.ivyManager = ivyManager;
    }

    public void setRepositoryAttributes(RepositoryAttributes repositoryAttributes)
    {
        this.repositoryAttributes = repositoryAttributes;
    }

    public void setScmClientFactory(MasterScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }

    public void setConfigurationVariableProvider(ConfigurationVariableProvider configurationVariableProvider)
    {
        this.configurationVariableProvider = configurationVariableProvider;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    private static class ContainsMatchingPropertyPredicate implements Predicate<ResourceProperty>
    {
        private final List<ResourceProperty> properties;

        public ContainsMatchingPropertyPredicate(List<ResourceProperty> properties)
        {
            this.properties = properties;
        }

        public boolean apply(final ResourceProperty commonProperty)
        {
            return properties.contains(commonProperty);
        }
    }


    private static class AssignmentRequestPriorityComparator implements Comparator<RecipeController>
    {
        public int compare(RecipeController controller1, RecipeController controller2)
        {
            int priority1 = controller1.getAssignmentRequest().getPriority();
            int priority2 = controller2.getAssignmentRequest().getPriority();
            if (priority1 > priority2)
            {
                return -1;
            }
            else if (priority1 < priority2)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    }
}
