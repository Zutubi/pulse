package com.zutubi.pulse.master.build.control;

import com.zutubi.events.AsynchronousDelegatingListener;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.dependency.RepositoryAttributes;
import com.zutubi.pulse.core.dependency.ivy.*;
import com.zutubi.pulse.core.engine.PulseFileProvider;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.events.RecipeCommencedEvent;
import com.zutubi.pulse.core.events.RecipeCompletedEvent;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.core.events.RecipeEvent;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.MasterBuildProperties;
import static com.zutubi.pulse.master.MasterBuildProperties.addRevisionProperties;
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.master.build.log.BuildLogFile;
import com.zutubi.pulse.master.build.log.DefaultBuildLogger;
import com.zutubi.pulse.master.build.log.DefaultRecipeLogger;
import com.zutubi.pulse.master.build.log.RecipeLogFile;
import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.build.queue.RecipeQueue;
import com.zutubi.pulse.master.dependency.ivy.ModuleDescriptorFactory;
import com.zutubi.pulse.master.events.build.*;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.scheduling.CallbackService;
import static com.zutubi.pulse.master.scm.ScmClientUtils.*;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.security.RepositoryAuthenticationProvider;
import com.zutubi.pulse.master.tove.config.project.*;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookManager;
import com.zutubi.pulse.servercore.CheckoutBootstrapper;
import com.zutubi.pulse.servercore.PatchBootstrapper;
import com.zutubi.pulse.servercore.ProjectRepoBootstrapper;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.*;
import static com.zutubi.util.StringUtils.safeToString;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

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
    private TestManager testManager;
    private MasterLocationProvider masterLocationProvider;
    private MasterConfigurationManager configurationManager;
    private RecipeQueue recipeQueue;
    private RecipeResultCollector collector;
    private BuildTree tree;
    private BuildResult buildResult;
    private AsynchronousDelegatingListener asyncListener;
    private List<TreeNode<RecipeController>> executingControllers = new LinkedList<TreeNode<RecipeController>>();
    private int pendingRecipes = 0;
    private CallbackService callbackService;

    private BuildResult previousSuccessful;
    private PulseExecutionContext buildContext;
    private BuildHookManager buildHookManager;
    private RepositoryAuthenticationProvider repositoryAuthenticationProvider;

    private ScmManager scmManager;
    private ThreadFactory threadFactory;
    private ResourceManager resourceManager;

    private DefaultBuildLogger buildLogger;
    private RecipeDispatchService recipeDispatchService;

    private IvyManager ivyManager;
    private RepositoryAttributes repositoryAttributes;
    private ModuleDescriptorFactory moduleDescriptorFactory;
    /**
     * A map of the recipe timeout callbacks.
     */
    private Map<Long, RecipeTimeoutCallback> timeoutCallbacks = new HashMap<Long, RecipeTimeoutCallback>();

    public DefaultBuildController(BuildRequestEvent event)
    {
        this.request = event;
        projectConfig = request.getProjectConfig();
    }

    public long start()
    {
        asyncListener = new AsynchronousDelegatingListener(this, threadFactory);
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

        // now that we have a persistent build result, all errors will be reported against
        // the result and a full cleanup is required.
        try
        {
            project = projectManager.getProject(projectConfig.getProjectId(), false);
            moduleDescriptorFactory = new ModuleDescriptorFactory(new IvyConfiguration(), configurationManager);
            previousSuccessful = buildManager.getLatestSuccessfulBuildResult(project);

            MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
            File buildDir = paths.getBuildDir(buildResult);
            buildResult.setAbsoluteOutputDir(configurationManager.getDataDirectory(), buildDir);

            buildLogger = new DefaultBuildLogger(new BuildLogFile(buildResult, paths));

            buildContext = new PulseExecutionContext();
            MasterBuildProperties.addProjectProperties(buildContext, projectConfig);
            MasterBuildProperties.addBuildProperties(buildContext, buildResult, project, buildDir, masterLocationProvider.getMasterUrl());
            buildContext.addValue(NAMESPACE_INTERNAL, PROPERTY_DEPENDENCY_DESCRIPTOR, createModuleDescriptor(projectConfig).getDescriptor());
            buildContext.addString(NAMESPACE_INTERNAL, PROPERTY_RETRIEVAL_PATTERN, projectConfig.getDependencies().getRetrievalPattern());
            buildContext.addString(NAMESPACE_INTERNAL, PROPERTY_SYNC_DESTINATION, Boolean.toString(projectConfig.getDependencies().isSyncDestination()));

            buildContext.addValue(NAMESPACE_INTERNAL, PROPERTY_SCM_CONFIGURATION, projectConfig.getScm());
            for (ResourceProperty requestProperty : asResourceProperties(request.getProperties()))
            {
                buildContext.add(requestProperty);
            }
            activateBuildAuthenticationToken();
            tree = new BuildTree();
            configure(tree.getRoot(), buildResult.getRoot());

            buildResult.queue();
            buildManager.save(buildResult);

            // We handle this event ourselves: this ensures that all processing of
            // the build from this point forth is handled by the single thread in
            // our async listener.  Basically, given events could be coming from
            // anywhere, even for different builds, it is much safer to ensure we
            // *only* use that thread after we have registered the listener.
            eventManager.register(asyncListener);

            // handle the event directly, there is no need to expose this event to the wider audience.
            asyncListener.handleEvent(new BuildControllerBootstrapEvent(this, buildResult, buildContext));
        }
        catch (Exception e)
        {
            // handle the event directly, there is no need to expose this event to the wider audience.
            asyncListener.handleEvent(new BuildControllerBootstrapEvent(this, buildResult, buildContext, e));
        }
        return buildResult.getNumber();
    }

    /**
     * To each build context we add a token that can later be used by any of the builds processes to
     * access the internal pulse artifact repository.  This token will be valid for the duration of the
     * build.
     */
    private void activateBuildAuthenticationToken()
    {
        String token = RandomUtils.randomToken(15);
        buildContext.setSecurityHash(token);
        repositoryAuthenticationProvider.activate(token);
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

    private void configure(TreeNode<RecipeController> rcNode, RecipeResultNode resultNode)
    {
        PulseFileProvider pulseFileProvider = getPulseFileSource();

        for (BuildStageConfiguration stageConfig : projectConfig.getStages().values())
        {
            RecipeResult recipeResult = new RecipeResult(stageConfig.getRecipe());
            RecipeResultNode childResultNode = new RecipeResultNode(stageConfig, recipeResult);
            resultNode.addChild(childResultNode);
            buildManager.save(resultNode);

            MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
            DefaultRecipeLogger logger = new DefaultRecipeLogger(new RecipeLogFile(buildResult, recipeResult.getId(), paths));

            if (stageConfig.isEnabled())
            {
                File recipeOutputDir = paths.getOutputDir(buildResult, recipeResult.getId());
                recipeResult.setAbsoluteOutputDir(configurationManager.getDataDirectory(), recipeOutputDir);

                PulseExecutionContext recipeContext = new PulseExecutionContext(buildContext);
                recipeContext.push();
                recipeContext.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE_ID, Long.toString(recipeResult.getId()));
                recipeContext.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE, stageConfig.getRecipe());
                recipeContext.addString(NAMESPACE_INTERNAL, PROPERTY_STAGE, stageConfig.getName());
                recipeContext.addValue(NAMESPACE_INTERNAL, PROPERTY_STAGE_HANDLE, stageConfig.getHandle());

                RecipeRequest recipeRequest = new RecipeRequest(new PulseExecutionContext(recipeContext));
                recipeRequest.setPulseFileSource(pulseFileProvider);
                List<ResourceRequirement> resourceRequirements = getResourceRequirements(stageConfig);
                recipeRequest.addAllResourceRequirements(resourceRequirements);
                recipeRequest.addAllProperties(asResourceProperties(projectConfig.getProperties().values()));
                recipeRequest.addAllProperties(asResourceProperties(stageConfig.getProperties().values()));
                recipeRequest.addAllProperties(asResourceProperties(request.getProperties()));

                RecipeAssignmentRequest assignmentRequest = new RecipeAssignmentRequest(project, getAgentRequirements(stageConfig), resourceRequirements, request.getRevision(), recipeRequest, buildResult);
                RecipeResultNode previousRecipe = previousSuccessful == null ? null : previousSuccessful.findResultNodeByHandle(stageConfig.getHandle());
                RecipeController rc = new RecipeController(projectConfig, buildResult, childResultNode, assignmentRequest, recipeContext, previousRecipe, logger, collector);
                rc.setRecipeQueue(recipeQueue);
                rc.setBuildManager(buildManager);
                rc.setEventManager(eventManager);
                rc.setBuildHookManager(buildHookManager);
                rc.setConfigurationManager(configurationManager);
                rc.setResourceManager(resourceManager);
                rc.setRecipeDispatchService(recipeDispatchService);
                rc.setScmManager(scmManager);

                TreeNode<RecipeController> child = new TreeNode<RecipeController>(rc);
                rcNode.add(child);
                pendingRecipes++;
            }
            else
            {
                recipeResult.skip();
                buildManager.save(recipeResult);
            }
        }
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

    private IvyModuleDescriptor createModuleDescriptor(ProjectConfiguration project)
    {
        IvyModuleDescriptor descriptor = moduleDescriptorFactory.createRetrieveDescriptor(project, buildResult);
        descriptor.setStatus(buildResult.getStatus());
        return descriptor;
    }

    private Collection<? extends ResourceProperty> asResourceProperties(Collection<ResourcePropertyConfiguration> resourcePropertyConfigurations)
    {
        return CollectionUtils.map(resourcePropertyConfigurations, new Mapping<ResourcePropertyConfiguration, ResourceProperty>()
        {
            public ResourceProperty map(ResourcePropertyConfiguration config)
            {
                return config.asResourceProperty();
            }
        });
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

    private List<ResourceRequirement> getResourceRequirements(BuildStageConfiguration node)
    {
        // get the list of resource requirements for the project AND the particular stage we are running.
        List<ResourceRequirement> requirements = new LinkedList<ResourceRequirement>();
        requirements.addAll(asResourceRequirements(projectConfig.getRequirements()));
        requirements.addAll(asResourceRequirements(node.getRequirements()));
        return requirements;
    }

    private Collection<? extends ResourceRequirement> asResourceRequirements(List<ResourceRequirementConfiguration> requirements)
    {
        return CollectionUtils.map(requirements, new Mapping<ResourceRequirementConfiguration, ResourceRequirement>()
        {
            public ResourceRequirement map(ResourceRequirementConfiguration config)
            {
                return config.asResourceRequirement();
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
            completeBuild();
        }
        catch (Exception e)
        {
            LOG.severe(e);
            buildResult.error();
            recordUnexpectedError(Feature.Level.ERROR, e, "Handling " + evt.getClass().getSimpleName());
            completeBuild();
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

        // delay propogating this startup exception until after the build directory
        // is avaiable to record the exception.
        if (e.hasStartupException())
        {
            throw new BuildException(e.getStartupException());
        }

        buildLogger.prepare();
        buildLogger.preBuild();
        publishEvent(new PreBuildEvent(this, buildResult, buildContext));
        buildLogger.preBuildCompleted();

        tree.prepare(buildResult);

        initialiseNodes(new BootstrapperCreator()
        {
            public Bootstrapper create()
            {
                Bootstrapper initialBootstrapper;
                if (buildContext.getBoolean(PROPERTY_INCREMENTAL_BOOTSTRAP, false))
                {
                    initialBootstrapper = new ProjectRepoBootstrapper(projectConfig.getName(), request.getRevision());
                }
                else
                {
                    initialBootstrapper = new CheckoutBootstrapper(projectConfig.getName(), request.getRevision());
                    if (request.isPersonal())
                    {
                        initialBootstrapper = createPersonalBuildBootstrapper(initialBootstrapper);
                    }
                }
                return initialBootstrapper;
            }
        }, tree.getRoot().getChildren());

        // If there are no executing controllers, then there is nothing more to be done.
        // Complete the build now as we will not be receiving event triggered callbacks
        // to complete the build.
        if (executingControllers.size() == 0)
        {
            completeBuild();
        }
    }

    private Revision getLatestRevision()
    {
        try
        {
            return withScmClient(projectConfig, scmManager, new ScmContextualAction<Revision>()
            {
                public Revision process(ScmClient client, ScmContext context) throws ScmException
                {
                    ScmContext c = (project.isInitialised()) ? context : null;
                    boolean supportsRevisions = client.getCapabilities(c).contains(ScmCapability.REVISIONS);
                    return supportsRevisions ? client.getLatestRevision(context) : new Revision(TimeStamps.getPrettyDate(System.currentTimeMillis(), Locale.getDefault()));
                }
            });
        }
        catch (ScmException e)
        {
            throw new BuildException("Unable to retrieve latest revision: " + e.getMessage(), e);
        }
    }

    private Bootstrapper createPersonalBuildBootstrapper(final Bootstrapper initialBootstrapper)
    {
        PersonalBuildRequestEvent pbr = ((PersonalBuildRequestEvent) request);
        return new PatchBootstrapper(initialBootstrapper, pbr.getUser().getId(), pbr.getNumber(), pbr.getPatchFormat());
    }

    private void handleBuildTerminationRequest(BuildTerminationRequestEvent event)
    {
        long id = event.getBuildId();

        if (id == buildResult.getId() || id == BuildTerminationRequestEvent.ALL_BUILDS)
        {
            terminateBuild(event.getMessage());
        }
    }

    private void terminateBuild(String message)
    {
        // Tell every running recipe to stop, and mark the build terminating
        // (so it will go into the error state on completion).
        buildResult.terminate(message);
        List<TreeNode<RecipeController>> completedNodes = new ArrayList<TreeNode<RecipeController>>(executingControllers.size());

        if (executingControllers.size() > 0)
        {
            for (TreeNode<RecipeController> controllerNode : executingControllers)
            {
                RecipeController controller = controllerNode.getData();
                controller.terminateRecipe("Terminated");
                if (checkControllerStatus(controller, false))
                {
                    completedNodes.add(controllerNode);
                }
            }

            buildManager.save(buildResult);
            executingControllers.removeAll(completedNodes);
        }

        if (executingControllers.size() == 0)
        {
            completeBuild();
        }
    }

    private void handleRecipeTimeout(RecipeTimeoutEvent event)
    {
        LOG.debug("Recipe timeout event received for build " + event.getBuildId() + ", recipe " + event.getRecipeId());
        TreeNode<RecipeController> found = null;
        for (TreeNode<RecipeController> controllerNode : executingControllers)
        {
            RecipeController controller = controllerNode.getData();
            if (controller.getResult().getId() == event.getRecipeId())
            {
                found = controllerNode;
                break;
            }
        }

        if (found != null)
        {
            LOG.debug("Terminating recipe for build " + event.getBuildId() + ", recipe " + event.getRecipeId());
            RecipeController controller = found.getData();
            controller.terminateRecipe("Timed out");
            if (checkControllerStatus(controller, false))
            {
                executingControllers.remove(found);
                if (executingControllers.size() == 0)
                {
                    completeBuild();
                }
                else
                {
                    checkForTermination(controller);
                }
            }
        }
    }

    private void initialiseNodes(BootstrapperCreator bootstrapperCreator, List<TreeNode<RecipeController>> nodes)
    {
        // Important to add them all first as a failure during initialisation
        // will test if there are other executing controllers (if not the
        // build is finished).
        for (TreeNode<RecipeController> node : nodes)
        {
            executingControllers.add(node);
        }

        for (TreeNode<RecipeController> node : nodes)
        {
            node.getData().initialise(bootstrapperCreator.create());
            checkNodeStatus(node);
        }
    }

    private void handleRecipeEvent(RecipeEvent e)
    {
        if (e instanceof RecipeCollectingEvent || e instanceof RecipeCollectedEvent)
        {
            // Ignore these.
            return;
        }

        RecipeController controller;
        TreeNode<RecipeController> foundNode = null;

        for (TreeNode<RecipeController> node : executingControllers)
        {
            controller = node.getData();
            if (controller.matchesRecipeEvent(e))
            {
                foundNode = node;
                break;
            }
        }

        if (foundNode != null)
        {
            // If we got here we are sure that the event was for one of our
            // recipes.
            if (e instanceof RecipeCommencedEvent)
            {
                pendingRecipes--;

                if (pendingRecipes == 0)
                {
                    handleLastCommenced();
                }

                if (projectConfig.getOptions().getTimeout() != BuildOptionsConfiguration.TIMEOUT_NEVER)
                {
                    scheduleTimeout(e.getRecipeId());
                }
            }
            else if (e instanceof RecipeAssignedEvent)
            {
                if (!buildResult.commenced())
                {
                    handleFirstAssignment();
                }
            }
            else if (e instanceof RecipeCompletedEvent || e instanceof RecipeErrorEvent)
            {
                try
                {
                    RecipeTimeoutCallback callback = timeoutCallbacks.get(e.getRecipeId());
                    if (callback != null)
                    {
                        callbackService.unregisterCallback(callback);
                    }
                }
                catch (Exception ex)
                {
                    LOG.warning("Unable to unschedule timeout trigger: " + ex.getMessage(), ex);
                }
            }

            foundNode.getData().handleRecipeEvent(e);
            checkNodeStatus(foundNode);
        }
    }

    private void handleLastCommenced()
    {
        // We can now make a more accurate estimate of our remaining running
        // time, as there are no more queued recipes.
        long longestRemaining = 0;

        for (RecipeController controller : tree)
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

        TimeStamps buildStamps = buildResult.getStamps();
        long estimatedEnd = System.currentTimeMillis() + longestRemaining;
        if (estimatedEnd > buildStamps.getStartTime())
        {
            buildStamps.setEstimatedRunningTime(estimatedEnd - buildStamps.getStartTime());
        }
    }

    /**
     * Called when the first recipe for this build is dispatched.  It is at
     * this point that the build is said to have commenced.
     */
    private void handleFirstAssignment()
    {
        BuildRevision buildRevision = request.getRevision();

        if (!buildRevision.isInitialised())
        {
            buildLogger.status("Initialising build revision...");
            buildRevision.setRevision(getLatestRevision());
            buildLogger.status("Revision initialised to '" + buildRevision.getRevision().getRevisionString() + "'");
        }

        getChanges(buildRevision);
        buildResult.commence();
        buildLogger.commenced(buildResult);

        addRevisionProperties(buildContext, buildResult);

        String version = request.getVersion();
        if (request.getOptions().isResolveVersion())
        {
            version = buildContext.resolveVariables(version);
        }
        buildResult.setVersion(version);

        buildContext.addValue(NAMESPACE_INTERNAL, PROPERTY_BUILD_VERSION, buildResult.getVersion());

        if (previousSuccessful != null)
        {
            buildResult.getStamps().setEstimatedRunningTime(previousSuccessful.getStamps().getElapsed());
        }
        buildManager.save(buildResult);
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
                    Set<ScmCapability> capabilities = getCapabilities(project, projectConfig, scmManager);
                    if (capabilities.contains(ScmCapability.CHANGESETS))
                    {
                        ScmContext context = scmManager.createContext(projectConfig);
                        client = scmManager.createClient(scm);

                        List<Changelist> scmChanges = client.getChanges(context, previousRevision, revision);

                        for (Changelist changelist : scmChanges)
                        {
                            PersistentChangelist persistentChangelist = new PersistentChangelist(changelist);
                            persistentChangelist.setProjectId(buildResult.getProject().getId());
                            persistentChangelist.setResultId(buildResult.getId());
                            buildManager.save(persistentChangelist);
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

    private void scheduleTimeout(long recipeId)
    {
        Date timeoutAt = new Date(System.currentTimeMillis() + projectConfig.getOptions().getTimeout() * Constants.MINUTE);

        try
        {
            RecipeTimeoutCallback timeoutCallback = new RecipeTimeoutCallback(buildResult.getId(), recipeId);
            timeoutCallbacks.put(recipeId, timeoutCallback);
            callbackService.registerCallback(timeoutCallback, timeoutAt);
        }
        catch (Exception e)
        {
            LOG.severe("Unable to schedule build timeout callback: " + e.getMessage(), e);
        }
    }

    private boolean checkControllerStatus(RecipeController controller, boolean collect)
    {
        if (controller.isFinished())
        {
            publishEvent(new RecipeCollectingEvent(this, controller.getResult().getId()));

            if (collect)
            {
                controller.collect(buildResult);
            }

            controller.cleanup(buildResult);
            controller.sendPostStageEvent();

            publishEvent(new RecipeCollectedEvent(this, controller.getResult().getId()));
            return true;
        }

        return false;
    }

    private void checkNodeStatus(TreeNode<RecipeController> node)
    {
        final RecipeController controller = node.getData();
        if (checkControllerStatus(controller, true))
        {
            executingControllers.remove(node);

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

            if (executingControllers.size() == 0)
            {
                completeBuild();
            }
            else
            {
                checkForTermination(controller);
            }
        }
    }

    private void checkForTermination(RecipeController controller)
    {
        RecipeResultNode recipeResultNode = controller.getResultNode();
        if (!recipeResultNode.getResult().succeeded())
        {
            String stageName = recipeResultNode.getStageName();
            if (projectConfig.getStage(stageName).isTerminateBuildOnFailure())
            {
                terminateBuild(I18N.format("terminate.stage.failure", stageName));
            }
            else if (stageFailureLimitReached())
            {
                terminateBuild(I18N.format("terminate.multiple.failures", projectConfig.getOptions().getStageFailureLimit()));
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
            buildResult.getRoot().forEachNode(new UnaryProcedure<RecipeResultNode>()
            {
                public void run(RecipeResultNode recipeResultNode)
                {
                    RecipeResult result = recipeResultNode.getResult();
                    if (result != null && result.completed() && !result.succeeded())
                    {
                        failures[0]++;
                    }
                }
            });

            return limit == failures[0];
        }
    }

    private void completeBuild()
    {
        abortUnfinishedRecipes();

        // If there is an SQL problem while saving the build result, the build becomes stuck and the server
        // needs to be restarted to clear it up.  To prevent the need for server restarts, we catch and log the exception
        // and continue.  This leaves the build result in an incorrect state, but will allow builds to continue. The
        // builds will be cleaned up next time the server restarts.  THIS IS ONLY A TEMPORARY FIX UNTIL WE WORK OUT
        // WHAT IS CAUSING THE SQL PROBLEMS (DEADLOCKS, STALE SESSIONS) IN THE FIRST PLACE.
        // Unfortunately, if we can not write to the db, then we are a little stuffed.
        try
        {
            if (buildResult.getRoot().getWorstState(ResultState.SUCCESS) == ResultState.SUCCESS && !buildResult.isPersonal())
            {
                try
                {
                    // publish this builds ivy file to the repository, making its artifacts available
                    // to subsequent builds.
                    publishIvyToRepository();
                }
                catch (Exception e)
                {
                    buildResult.error(new BuildException(e));
                }
            }

            buildResult.complete();
            buildLogger.completed(buildResult);

            // The timing of this event is important: handlers of this event
            // are allowed to add information to and modify the state of the
            // build result.  Hence it is crucial that indexing and a final
            // save are done afterwards.
            MasterBuildProperties.addCompletedBuildProperties(buildContext, buildResult, configurationManager);
            buildLogger.postBuild();
            publishEvent(new PostBuildEvent(this, buildResult, buildContext));
            buildLogger.postBuildCompleted();

            // calculate the feature counts at the end of the build so that the result hierarchy does not need to
            // be traversed when this information is required.
            buildResult.calculateFeatureCounts();

            long start = System.currentTimeMillis();
            testManager.index(buildResult);
            long duration = System.currentTimeMillis() - start;
            if (duration > 300000)
            {
                LOG.warning("Test case indexing for project %s took %f seconds", projectConfig.getName(), duration / 1000.0);
            }

            buildManager.save(buildResult);
        }
        catch (Exception e)
        {
            LOG.severe("Failed to persist the completed build result. Reason: " + e.getMessage(), e);
        }

        deactivateBuildAuthenticationToken();

        eventManager.unregister(asyncListener);
        publishEvent(new BuildCompletedEvent(this, buildResult, buildContext));

        buildLogger.close();

        // this must be last since we are in fact stopping the thread running this method.., we are
        // after all responding to an event on this listener.
        asyncListener.stop(true);
    }

    /**
     * Publish an ivy file to the repository.
     */
    private void publishIvyToRepository()
    {
        buildLogger.preIvyPublish();
        try
        {
            String version = buildContext.getString(PROPERTY_BUILD_VERSION);

            final IvyModuleDescriptor descriptor = moduleDescriptorFactory.createDescriptor(projectConfig, buildResult, version);
            descriptor.setBuildNumber(buildResult.getNumber());

            long projectHandle = buildContext.getLong(PROPERTY_PROJECT_HANDLE, 0);
            if (projectHandle != 0)
            {
                String path = descriptor.getPath();
                repositoryAttributes.addAttribute(PathUtils.getParentPath(path), RepositoryAttributes.PROJECT_HANDLE, String.valueOf(projectHandle));
            }

            String masterUrl = buildContext.getString(PROPERTY_MASTER_URL);
            String repositoryUrl = masterUrl + WebManager.REPOSITORY_PATH;

            final IvyClient ivy = ivyManager.createIvyClient(repositoryUrl);
            ivy.pushMessageLogger(buildLogger.getMessageLogger());

            String host = new URL(masterUrl).getHost();
            String password = buildContext.getSecurityHash();

            AuthenticatedAction.execute(host, password, new NullaryFunctionE<Object, Exception>()
            {
                public Object process() throws Exception
                {
                    ivy.publishArtifacts(descriptor);
                    ivy.publishDescriptor(descriptor);
                    return null;
                }
            });
        }
        catch (Exception e)
        {
            throw new BuildException("Failed to publish the build's ivy file to the repository. Cause: " + e.getMessage(), e);
        }
        finally
        {
            buildLogger.postIvyPublish();
        }
    }

    private void abortUnfinishedRecipes()
    {
        buildResult.abortUnfinishedRecipes();
        for (TreeNode<RecipeController> controllerNode : executingControllers)
        {
            eventManager.publish(new RecipeAbortedEvent(this, controllerNode.getData().getResult().getId()));
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildStatusEvent.class, RecipeEvent.class, BuildTerminationRequestEvent.class, RecipeTimeoutEvent.class};
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

    public void setTestManager(TestManager testManager)
    {
        this.testManager = testManager;
    }

    public void setRecipeQueue(RecipeQueue recipeQueue)
    {
        this.recipeQueue = recipeQueue;
    }

    public void setCollector(RecipeResultCollector collector)
    {
        this.collector = collector;
    }

    public void setCallbackService(CallbackService callbackService)
    {
        this.callbackService = callbackService;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }

    public void setBuildHookManager(BuildHookManager buildHookManager)
    {
        this.buildHookManager = buildHookManager;
    }

    public void setRecipeDispatchService(RecipeDispatchService recipeDispatchService)
    {
        this.recipeDispatchService = recipeDispatchService;
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

    private static interface BootstrapperCreator
    {
        Bootstrapper create();
    }

    private class RecipeTimeoutCallback implements NullaryProcedure
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
            eventManager.publish(new RecipeTimeoutEvent(DefaultBuildController.this, buildResultId, recipeId));
        }
    }
}
