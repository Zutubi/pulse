package com.zutubi.pulse.master;

import com.zutubi.events.AsynchronousDelegatingListener;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.dependency.RepositoryAttributes;
import com.zutubi.pulse.core.dependency.ivy.AuthenticatedAction;
import com.zutubi.pulse.core.dependency.ivy.IvyClient;
import com.zutubi.pulse.core.dependency.ivy.IvyManager;
import com.zutubi.pulse.core.dependency.ivy.IvyModuleRevisionId;
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
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.master.dependency.ivy.ModuleDescriptorFactory;
import com.zutubi.pulse.master.events.build.*;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.scheduling.quartz.TimeoutRecipeJob;
import static com.zutubi.pulse.master.scm.ScmClientUtils.*;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.security.RepositoryAuthenticationProvider;
import com.zutubi.pulse.master.tove.config.project.*;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookManager;
import com.zutubi.pulse.servercore.CheckoutBootstrapper;
import com.zutubi.pulse.servercore.PatchBootstrapper;
import com.zutubi.pulse.servercore.ProjectRepoBootstrapper;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.*;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.util.url.CredentialsStore;
import org.quartz.*;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ThreadFactory;

/**
 * The BuildController is responsible for executing and coordinating a single
 * build request.
 */
public class BuildController implements EventListener, BuildHandler
{
    private static final Logger LOG = Logger.getLogger(BuildController.class);

    public static final String TIMEOUT_JOB_NAME = "build";
    public static final String TIMEOUT_JOB_GROUP = "timeout";
    private static final String TIMEOUT_TRIGGER_GROUP = "timeout";

    private AbstractBuildRequestEvent request;
    private Project project;
    private ProjectConfiguration projectConfig;

    private EventManager eventManager;
    private ProjectManager projectManager;
    private UserManager userManager;
    private BuildManager buildManager;
    private TestManager testManager;
    private MasterLocationProvider masterLocationProvider;
    private MasterConfigurationManager configurationManager;
    private RecipeQueue recipeQueue;
    private RecipeResultCollector collector;
    private BuildTree tree;
    private BuildResult buildResult;
    private File buildDir;
    private AsynchronousDelegatingListener asyncListener;
    private List<TreeNode<RecipeController>> executingControllers = new LinkedList<TreeNode<RecipeController>>();
    private int pendingRecipes = 0;
    private Scheduler quartzScheduler;
    private ServiceTokenManager serviceTokenManager;
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
    private ModuleDescriptorFactory moduleDescriptorFactory = new ModuleDescriptorFactory();

    public BuildController(AbstractBuildRequestEvent event)
    {
        this.request = event;
        projectConfig = request.getProjectConfig();
    }

    public void run()
    {
        project = projectManager.getProject(projectConfig.getProjectId(), false);
        asyncListener = new AsynchronousDelegatingListener(this, threadFactory);

        try
        {
            JobDetail detail = quartzScheduler.getJobDetail(TIMEOUT_JOB_NAME, TIMEOUT_JOB_GROUP);
            if (detail ==  null)
            {
                detail = new JobDetail(TIMEOUT_JOB_NAME, TIMEOUT_JOB_GROUP, TimeoutRecipeJob.class);
                detail.setDurability(true); // will stay around after the trigger has gone.
                quartzScheduler.addJob(detail, true);
            }
        }
        catch (SchedulerException e)
        {
            LOG.severe("Unable to setup build timeout job: " + e.getMessage(), e);
        }

        createBuildTree();

        // Fail early if things are not as expected.
        if (!buildResult.isPersistent())
        {
            throw new RuntimeException("Build result must be a persistent instance.");
        }

        buildResult.setAbsoluteOutputDir(configurationManager.getDataDirectory(), buildDir);
        buildResult.queue();
        buildManager.save(buildResult);

        // We handle this event ourselves: this ensures that all processing of
        // the build from this point forth is handled by the single thread in
        // our async listener.  Basically, given events could be coming from
        // anywhere, even for different builds, it is much safer to ensure we
        // *only* use that thread after we have registered the listener.
        eventManager.register(asyncListener);
        publishEvent(new BuildControllerBootstrapEvent(this, buildResult, buildContext));
    }

    public BuildTree createBuildTree()
    {
        tree = new BuildTree();

        TreeNode<RecipeController> root = tree.getRoot();
        buildResult = request.createResult(projectManager, userManager);
        buildManager.save(buildResult);
        previousSuccessful = buildManager.getLatestSuccessfulBuildResult(project);

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        buildDir = paths.getBuildDir(buildResult);

        buildLogger = new DefaultBuildLogger(new File(buildDir, BuildResult.BUILD_LOG));

        buildContext = new PulseExecutionContext();
        MasterBuildProperties.addProjectProperties(buildContext, projectConfig);
        MasterBuildProperties.addBuildProperties(buildContext, buildResult, project, buildDir, masterLocationProvider.getMasterUrl());
        buildContext.addValue(NAMESPACE_INTERNAL, PROPERTY_DEPENDENCY_DESCRIPTOR, createModuleDescriptor(projectConfig));
        buildContext.addValue(NAMESPACE_INTERNAL, PROPERTY_SCM_CONFIGURATION, projectConfig.getScm());
        for (ResourceProperty requestProperty : asResourceProperties(request.getProperties()))
        {
            buildContext.add(requestProperty);
        }

        String version = projectConfig.getDependencies().getVersion();
        TriggerOptions options = request.getOptions();
        if (options.hasVersion())
        {
            version = options.getVersion();
        }
        if (options.isResolveVersion())
        {
            version = buildContext.resolveReferences(version);
        }
        buildContext.addValue(NAMESPACE_INTERNAL, PROPERTY_BUILD_VERSION, version);
        buildResult.setVersion(version);

        activateBuildAuthenticationToken();

        configure(root, buildResult.getRoot());

        return tree;
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
            File recipeOutputDir = paths.getOutputDir(buildResult, recipeResult.getId());
            recipeResult.setAbsoluteOutputDir(configurationManager.getDataDirectory(), recipeOutputDir);

            PulseExecutionContext recipeContext = new PulseExecutionContext(buildContext);
            recipeContext.push();
            recipeContext.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE_ID, Long.toString(recipeResult.getId()));
            recipeContext.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE, stageConfig.getRecipe());
            recipeContext.addString(NAMESPACE_INTERNAL, PROPERTY_STAGE, stageConfig.getName());

            String retrievalPattern = projectConfig.getDependencies().getRetrievalPattern();
            recipeContext.addString(NAMESPACE_INTERNAL, PROPERTY_RETRIEVAL_PATTERN, retrievalPattern);

            RecipeRequest recipeRequest = new RecipeRequest(new PulseExecutionContext(recipeContext));
            recipeRequest.setPulseFileSource(pulseFileProvider);
            List<ResourceRequirement> resourceRequirements = getResourceRequirements(stageConfig);
            recipeRequest.addAllResourceRequirements(resourceRequirements);
            recipeRequest.addAllProperties(asResourceProperties(projectConfig.getProperties().values()));
            recipeRequest.addAllProperties(asResourceProperties(request.getOptions().getProperties()));
            recipeRequest.addAllProperties(asResourceProperties(stageConfig.getProperties().values()));

            RecipeAssignmentRequest assignmentRequest = new RecipeAssignmentRequest(project, getAgentRequirements(stageConfig), resourceRequirements, request.getRevision(), recipeRequest, buildResult);
            DefaultRecipeLogger logger = new DefaultRecipeLogger(new File(paths.getRecipeDir(buildResult, recipeResult.getId()), RecipeResult.RECIPE_LOG));
            RecipeResultNode previousRecipe = previousSuccessful == null ? null : previousSuccessful.findResultNodeByHandle(stageConfig.getHandle());
            RecipeController rc = new RecipeController(projectConfig, buildResult, childResultNode, assignmentRequest, recipeContext, previousRecipe, logger, collector);
            rc.setRecipeQueue(recipeQueue);
            rc.setBuildManager(buildManager);
            rc.setServiceTokenManager(serviceTokenManager);
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

    private ModuleDescriptor createModuleDescriptor(ProjectConfiguration project)
    {
        ModuleDescriptorFactory f = new ModuleDescriptorFactory();
        DefaultModuleDescriptor descriptor = f.createDescriptor(project);
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
        try
        {
            if (evt instanceof BuildControllerBootstrapEvent)
            {
                BuildControllerBootstrapEvent e = (BuildControllerBootstrapEvent) evt;
                if (e.getBuildResult() == buildResult)
                {
                    handleControllerBootstrap();
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
            buildResult.error("Unexpected error: " + e.getMessage());
            completeBuild();
        }
    }

    private void handleControllerBootstrap()
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

        buildLogger.prepare();
        buildLogger.preBuild();
        publishEvent(new PreBuildEvent(this, buildResult, buildContext));
        buildLogger.preBuildCompleted();

        initialiseRevision();
        tree.prepare(buildResult);

        // execute the first level of recipe controllers...
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
    }

    private void initialiseRevision()
    {
        BuildRevision buildRevision = request.getRevision();
        buildRevision.lock();
        try
        {
            if (!buildRevision.isInitialised())
            {
                buildLogger.status("Initialising build revision...");
                updateRevision(buildRevision, null);
                buildLogger.status("Revision initialised to '" + buildRevision.getRevision().getRevisionString() + "'");
            }
        }
        finally
        {
            buildRevision.unlock();
        }
    }

    /**
     * Updates an active build's revision to the new revision.  This is
     * allowed up until the point where the revision is fixed (which is at the
     * same time as the build commences).
     * <p/>
     * The build revision for this build <strong>must</strong> be locked before
     * making this call.
     *
     * @param revision the new revision, which may be null to indicate that
     *                 the latest revision should be used
     * @return true iff the revision was updated
     * @throws BuildException if the revision cannot be set due to an error
     */
    public boolean updateRevisionIfNotFixed(Revision revision)
    {
        boolean updated = false;

        BuildRevision buildRevision = request.getRevision();
        buildRevision.lock();
        try
        {
            if (!buildRevision.isFixed())
            {
                updateRevision(buildRevision, revision);
                updated = true;
            }
        }
        finally
        {
            buildRevision.unlock();
        }

        if (updated)
        {
            buildLogger.status("Revision updated to '" + buildRevision.getRevision() + "' due to a newer build request");
            eventManager.publish(new BuildRevisionUpdatedEvent(this, buildResult, buildRevision));
        }

        return updated;
    }

    private void updateRevision(BuildRevision buildRevision, Revision revision)
    {
        if (revision == null)
        {
            revision = getLatestRevision();
        }

        if (revision.equals(buildRevision.getRevision()))
        {
            return;
        }

        buildRevision.update(revision);
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
        // TODO: preferrable to move this out (maybe to the request)
        try
        {
            return withScmClient(projectConfig, scmManager, new ScmContextualAction<Bootstrapper>()
            {
                public Bootstrapper process(ScmClient client, ScmContext context) throws ScmException
                {
                    PersonalBuildRequestEvent pbr = ((PersonalBuildRequestEvent) request);
                    EOLStyle localEOL = client.getEOLPolicy(context);
                    return new PatchBootstrapper(initialBootstrapper, pbr.getUser().getId(), pbr.getNumber(), pbr.getPatchFormat(), localEOL);
                }
            });
        }
        catch (ScmException e)
        {
            throw new BuildException("Unable to determine SCM end-of-line policy: " + e.getMessage(), e);
        }
    }

    private String getTriggerName(long recipeId)
    {
        return String.format("recipe-%d", recipeId);
    }

    private void handleBuildTerminationRequest(BuildTerminationRequestEvent event)
    {
        long id = event.getBuildId();

        if (id == buildResult.getId() || id == BuildTerminationRequestEvent.ALL_BUILDS)
        {
            // Tell every running recipe to stop, and mark the build terminating
            // (so it will go into the error state on completion).
            buildResult.terminate(event.getMessage());
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
    }

    private void handleRecipeTimeout(RecipeTimeoutEvent event)
    {
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
            RecipeController controller = found.getData();
            controller.terminateRecipe("Timed out");
            if (checkControllerStatus(controller, false))
            {
                executingControllers.remove(found);
                if (executingControllers.size() == 0)
                {
                    completeBuild();
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
                    // during a system shutdown, the scheduler is shutdown before the
                    // builds are completed. This makes it unnecessary to unschedule the job.
                    if (!quartzScheduler.isShutdown())
                    {
                        quartzScheduler.unscheduleJob(getTriggerName(e.getRecipeId()), TIMEOUT_TRIGGER_GROUP);
                    }
                }
                catch (SchedulerException ex)
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

        MasterBuildProperties.addRevisionProperties(buildContext, buildRevision);
        getChanges(buildRevision);

        buildResult.commence(buildRevision.getTimestamp());
        buildLogger.commenced(buildResult);
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
                finally
                {
                    IOUtils.close(client);
                }
            }
        }
    }

    private void scheduleTimeout(long recipeId)
    {
        String name = getTriggerName(recipeId);
        Date time = new Date(System.currentTimeMillis() + projectConfig.getOptions().getTimeout() * Constants.MINUTE);

        Trigger timeoutTrigger = new SimpleTrigger(name, TIMEOUT_TRIGGER_GROUP, time);
        timeoutTrigger.setJobName(TIMEOUT_JOB_NAME);
        timeoutTrigger.setJobGroup(TIMEOUT_JOB_GROUP);
        timeoutTrigger.getJobDataMap().put(TimeoutRecipeJob.PARAM_BUILD_ID, buildResult.getId());
        timeoutTrigger.getJobDataMap().put(TimeoutRecipeJob.PARAM_RECIPE_ID, recipeId);

        try
        {
            quartzScheduler.scheduleJob(timeoutTrigger);
        }
        catch (SchedulerException e)
        {
            LOG.severe("Unable to schedule build timeout trigger: " + e.getMessage(), e);
        }
    }

    private boolean checkControllerStatus(RecipeController controller, boolean collect)
    {
        if (controller.isFinished())
        {
            publishEvent(new RecipeCollectingEvent(this, controller.getResult().getId()));

            if (collect)
            {
                controller.collect(buildResult, projectConfig.getOptions().getRetainWorkingCopy());
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
            if (result.succeeded())
            {
                initialiseNodes(new BootstrapperCreator()
                {
                    public Bootstrapper create()
                    {
                        return controller.getChildBootstrapper();
                    }
                }, node.getChildren());
            }
            else if (result.failed())
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
            completeBuild();
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
            if (buildResult.getRoot().getWorstState(null) == ResultState.SUCCESS && !buildResult.isPersonal())
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

            buildResult.setHasWorkDir(projectConfig.getOptions().getRetainWorkingCopy());
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
            String masterUrl = buildContext.getString(PROPERTY_MASTER_URL);
            String repositoryUrl = masterUrl + WebManager.REPOSITORY_PATH;

            final IvyClient ivy = ivyManager.createIvyClient(repositoryUrl);
            ivy.setMessageLogger(buildLogger.getMessageLogger());

            String host = new URL(masterUrl).getHost();
            String password = buildContext.getSecurityHash();

            String user = AuthenticatedAction.USER;
            String realm = AuthenticatedAction.REALM;
            CredentialsStore.INSTANCE.addCredentials(realm, host, user, password);

            String version = buildContext.getString(PROPERTY_BUILD_VERSION);
            final ModuleRevisionId mrid = IvyModuleRevisionId.newInstance(projectConfig.getOrganisation(), projectConfig.getName(), version);

            // add projecthandle attribute to the repository.
            long projectHandle = buildContext.getLong(PROPERTY_PROJECT_HANDLE, 0);
            if (projectHandle != 0)
            {
                String path = ivy.getIvyPath(mrid, version);
                repositoryAttributes.addAttribute(PathUtils.getParentPath(path), RepositoryAttributes.PROJECT_HANDLE, String.valueOf(projectHandle));
            }

            DefaultModuleDescriptor descriptor = moduleDescriptorFactory.createDescriptor(projectConfig, buildResult, version, configurationManager);
            descriptor.setStatus(buildResult.getStatus());
            descriptor.addExtraInfo("buildNumber", String.valueOf(buildResult.getNumber()));

            ivy.publishArtifacts(descriptor, "[sourcefile]");

            ivy.resolve(descriptor);
            ivy.publishIvy(descriptor, version);
        }
        catch (Exception e)
        {
            throw new BuildException("Failed to publish the builds ivy file to the repository. Cause: " + e.getMessage(), e);
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
        return new Class[]{BuildControllerBootstrapEvent.class, BuildStatusEvent.class, RecipeEvent.class, BuildTerminationRequestEvent.class, RecipeTimeoutEvent.class};
    }

    public Project getProject()
    {
        return project;
    }

    public long getBuildId()
    {
        return buildResult.getId();
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

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
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

    public void setQuartzScheduler(Scheduler quartzScheduler)
    {
        this.quartzScheduler = quartzScheduler;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
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
}
