package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.scm.*;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.events.AsynchronousDelegatingListener;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.*;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.prototype.config.project.BuildOptionsConfiguration;
import com.zutubi.pulse.prototype.config.project.BuildStageConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.scheduling.quartz.TimeoutRecipeJob;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.TimeStamps;
import com.zutubi.pulse.util.TreeNode;
import com.zutubi.util.Constants;
import com.zutubi.util.logging.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public class BuildController implements EventListener
{
    private static final Logger LOG = Logger.getLogger(BuildController.class);

    private static final String TIMEOUT_TRIGGER_GROUP = "timeout";

    private static final Lock CHANGE_LOCK = new ReentrantLock();

    private AbstractBuildRequestEvent request;
    private Project project;
    private ProjectConfiguration projectConfig;

    private EventManager eventManager;
    private ProjectManager projectManager;
    private UserManager userManager;
    private BuildManager buildManager;
    private TestManager testManager;
    private MasterConfigurationManager configurationManager;
    private RecipeQueue queue;
    private RecipeResultCollector collector;
    private BuildTree tree;
    private BuildResult buildResult;
    private AsynchronousDelegatingListener asyncListener;
    private List<TreeNode<RecipeController>> executingControllers = new LinkedList<TreeNode<RecipeController>>();
    private int pendingRecipes = 0;
    private Scheduler quartzScheduler;
    private ServiceTokenManager serviceTokenManager;
    private BuildResult previousSuccessful;
    private List<ResourceProperty> buildProperties;

    private ScmClientFactory scmClientFactory;
    private ThreadFactory threadFactory;

    public BuildController(AbstractBuildRequestEvent event)
    {
        this.request = event;
    }

    public void init()
    {
        this.projectConfig = request.getProjectConfig();
        this.project = projectManager.getProject(projectConfig.getProjectId(), false);
        this.asyncListener = new AsynchronousDelegatingListener(this, threadFactory);
    }

    public void run()
    {
        createBuildTree();

        // Fail early if things are not as expected.
        if (!buildResult.isPersistent())
        {
            throw new RuntimeException("Build result must be a persistent instance.");
        }

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File buildDir = paths.getBuildDir(buildResult);
        buildResult.setAbsoluteOutputDir(configurationManager.getDataDirectory(), buildDir);
        buildResult.queue();
        buildManager.save(buildResult);

        eventManager.publish(new PreBuildEvent(this, buildResult));
        
        // We handle this event ourselves: this ensures that all processing of
        // the build from this point forth is handled by the single thread in
        // our async listener.  Basically, given events could be coming from
        // anywhere, even for different builds, it is much safer to ensure we
        // *only* use that thread after we have registered the listener.
        eventManager.register(asyncListener);
        eventManager.publish(new BuildCommencedEvent(this, buildResult));
    }

    public BuildTree createBuildTree()
    {
        tree = new BuildTree();

        TreeNode<RecipeController> root = tree.getRoot();
        buildResult = request.createResult(projectManager, userManager);
        buildManager.save(buildResult);
        previousSuccessful = getPreviousSuccessfulBuild();
        buildProperties = new LinkedList<ResourceProperty>(projectConfig.getProperties().values());
        buildProperties.add(new ResourceProperty("project", projectConfig.getName()));

        configure(root, buildResult.getRoot());

        return tree;
    }

    private BuildResult getPreviousSuccessfulBuild()
    {
        BuildResult previousSuccessful = null;
        List<BuildResult> previousSuccess = buildManager.queryBuilds(project, new ResultState[]{ResultState.SUCCESS}, -1, -1, 0, 1, true, true);
        if (previousSuccess.size() > 0)
        {
            previousSuccessful = previousSuccess.get(0);
        }
        return previousSuccessful;
    }

    private void configure(TreeNode<RecipeController> rcNode, RecipeResultNode resultNode)
    {
        for (BuildStageConfiguration stage : projectConfig.getStages().values())
        {
            RecipeResult recipeResult = new RecipeResult(stage.getRecipe());
            RecipeResultNode childResultNode = new RecipeResultNode(stage.getName(), stage.getHandle(), recipeResult);
            resultNode.addChild(childResultNode);
            buildManager.save(resultNode);

            MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
            File recipeOutputDir = paths.getOutputDir(buildResult, recipeResult.getId());
            recipeResult.setAbsoluteOutputDir(configurationManager.getDataDirectory(), recipeOutputDir);

            CheckoutScheme checkoutScheme = projectConfig.getScm().getCheckoutScheme();
            boolean incremental = !request.isPersonal() && checkoutScheme == CheckoutScheme.INCREMENTAL_UPDATE;
            List<ResourceProperty> recipeProperties = new LinkedList<ResourceProperty>(buildProperties);
            RecipeRequest recipeRequest = new RecipeRequest(projectConfig.getName(), recipeResult.getId(), stage.getRecipe(), incremental, true, projectConfig.getOptions().getRetainWorkingCopy(), getResourceRequirements(stage), recipeProperties);
            RecipeDispatchRequest dispatchRequest = new RecipeDispatchRequest(project, stage.getAgentRequirements(), request.getRevision(), recipeRequest, buildResult);
            DefaultRecipeLogger logger = new DefaultRecipeLogger(new File(paths.getRecipeDir(buildResult, recipeResult.getId()), RecipeResult.RECIPE_LOG));
            RecipeResultNode previousRecipe = previousSuccessful == null ? null : previousSuccessful.findResultNodeByHandle(stage.getHandle());
            RecipeController rc = new RecipeController(buildResult, childResultNode, dispatchRequest, incremental, previousRecipe, logger, collector);
            rc.setRecipeQueue(queue);
            rc.setBuildManager(buildManager);
            rc.setServiceTokenManager(serviceTokenManager);
            rc.setEventManager(eventManager);

            TreeNode<RecipeController> child = new TreeNode<RecipeController>(rc);
            rcNode.add(child);
            pendingRecipes++;
        }
    }

    private List<ResourceRequirement> getResourceRequirements(BuildStageConfiguration node)
    {
        // get the list of resource requirements for the project AND the particular stage we are running.
        List<ResourceRequirement> requirements = new LinkedList<ResourceRequirement>();
        requirements.addAll(projectConfig.getRequirements());
        requirements.addAll(node.getRequirements());
        return requirements;
    }

    public void handleEvent(Event evt)
    {
        // Add an event filter here... we are only interested in events that belong to the project
        // that we are dealing with. Q: How do we identify our events..
        try
        {
            if (evt instanceof BuildCommencedEvent)
            {
                BuildCommencedEvent e = (BuildCommencedEvent) evt;
                if (e.getBuildResult() == buildResult)
                {
                    handleBuildCommenced();
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
                RecipeEvent e = (RecipeEvent) evt;
                handleRecipeEvent(e);
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

    private void handleBuildCommenced()
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

        CheckoutScheme checkoutScheme = projectConfig.getScm().getCheckoutScheme();

        // check project configuration to determine which bootstrap configuration should be used.
        Bootstrapper initialBootstrapper;
        boolean checkoutOnly = request.isPersonal() || checkoutScheme == CheckoutScheme.CLEAN_CHECKOUT;
        if (checkoutOnly)
        {
            initialBootstrapper = new CheckoutBootstrapper(projectConfig.getName(), projectConfig.getScm(), request.getRevision(), false);
            if (request.isPersonal())
            {
                initialBootstrapper = createPersonalBuildBootstrapper(initialBootstrapper);
            }
        }
        else
        {
            initialBootstrapper = new ProjectRepoBootstrapper(projectConfig.getName(), projectConfig.getScm(), request.getRevision());
        }

        tree.prepare(buildResult);

        // execute the first level of recipe controllers...
        initialiseNodes(initialBootstrapper, tree.getRoot().getChildren());
    }

    private Bootstrapper createPersonalBuildBootstrapper(Bootstrapper initialBootstrapper)
    {
        // TODO: preferrable to move this out (maybe to the request)
        PersonalBuildRequestEvent pbr = ((PersonalBuildRequestEvent) request);
        ScmClient client = null;
        try
        {
            client = scmClientFactory.createClient(projectConfig.getScm());
            FileStatus.EOLStyle localEOL = client.getEOLPolicy();
            initialBootstrapper = new PatchBootstrapper(initialBootstrapper, pbr.getUser().getId(), pbr.getNumber(), localEOL);
        }
        catch (ScmException e)
        {
            throw new BuildException("Unable to determine SCM end-of-line policy: " + e.getMessage(), e);
        }
        finally
        {
            ScmClientUtils.close(client);
        }

        return initialBootstrapper;
    }

    private String getTriggerName(long recipeId)
    {
        return String.format("recipe-%d", recipeId);
    }

    private void handleBuildTerminationRequest(BuildTerminationRequestEvent event)
    {
        long id = event.getBuildId();

        if (id == buildResult.getId() || id == -1)
        {
            // Tell every running recipe to stop, and mark the build terminating
            // (so it will go into the error state on completion).
            buildResult.terminate(event.isTimeout());
            List<TreeNode<RecipeController>> completedNodes = new ArrayList<TreeNode<RecipeController>>(executingControllers.size());

            if (executingControllers.size() > 0)
            {
                for (TreeNode<RecipeController> controllerNode : executingControllers)
                {
                    RecipeController controller = controllerNode.getData();
                    controller.terminateRecipe(event.isTimeout());
                    if (controller.isFinished())
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
            controller.terminateRecipe(true);
            if (controller.isFinished())
            {
                executingControllers.remove(found);
                if (executingControllers.size() == 0)
                {
                    completeBuild();
                }
            }
        }
    }

    private void initialiseNodes(Bootstrapper bootstrapper, List<TreeNode<RecipeController>> nodes)
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
            node.getData().initialise(bootstrapper);
            checkNodeStatus(node);
        }
    }

    private void handleRecipeEvent(RecipeEvent e)
    {
        RecipeController controller;
        TreeNode<RecipeController> foundNode = null;

        for (TreeNode<RecipeController> node : executingControllers)
        {
            controller = node.getData();
            if (controller.handleRecipeEvent(e))
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
            else if (e instanceof RecipeDispatchedEvent)
            {
                if (!buildResult.commenced())
                {
                    handleFirstDispatch(foundNode.getData());
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

                if (e instanceof RecipeCompletedEvent)
                {
                    RecipeCompletedEvent completedEvent = (RecipeCompletedEvent) e;
                    String version = completedEvent.getBuildVersion();
                    if (version != null)
                    {
                        buildResult.setVersion(version);
                        buildManager.save(buildResult);
                    }
                }
            }

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
     *
     * @param controller the controller for the recipe that has been dispatched
     */
    private void handleFirstDispatch(RecipeController controller)
    {
        RecipeDispatchRequest dispatchRequest = controller.getDispatchRequest();
        RecipeRequest request = dispatchRequest.getRequest();

        try
        {
            FileSystemUtils.createFile(new File(buildResult.getAbsoluteOutputDir(configurationManager.getDataDirectory()), BuildResult.PULSE_FILE), request.getPulseFileSource());
        }
        catch (IOException e)
        {
            LOG.warning("Unable to save pulse file for build: " + e.getMessage(), e);
        }

        if (!buildResult.isPersonal())
        {
            getChanges(dispatchRequest.getRevision());
        }

        buildResult.commence(dispatchRequest.getRevision().getTimestamp());
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

        if (!buildResult.isUserRevision())
        {
            ScmConfiguration scm = projectConfig.getScm();
            Revision previousRevision = buildManager.getPreviousRevision(project);

            if (previousRevision != null)
            {
                ScmClient client = null;
                try
                {
                    client = scmClientFactory.createClient(scm);
                    getChangeSince(client, previousRevision, revision);
                }
                catch (ScmException e)
                {
                    LOG.warning("Unable to retrieve changelist details from SCM server: " + e.getMessage(), e);
                }
                finally
                {
                    ScmClientUtils.close(client);
                }
            }
        }
    }

    private List<Changelist> getChangeSince(ScmClient client, Revision previousRevision, Revision revision) throws ScmException
    {
        List<Changelist> result = new LinkedList<Changelist>();
        List<Changelist> scmChanges = client.getChanges(previousRevision, revision);

        // Get the uid after the changes as Svn requires a connection to be
        // made first
        String uid = client.getUid();

        CHANGE_LOCK.lock();
        try
        {
            for (Changelist change : scmChanges)
            {
                // Have we already got this revision?
                Changelist alreadySaved = buildManager.getChangelistByRevision(uid, change.getRevision());
                if (alreadySaved != null)
                {
                    change = alreadySaved;
                }

                change.addProjectId(buildResult.getProject().getId());
                change.addResultId(buildResult.getId());
                buildManager.save(change);
                result.add(change);
            }
        }
        finally
        {
            CHANGE_LOCK.unlock();
        }

        return result;
    }

    private void scheduleTimeout(long recipeId)
    {
        String name = getTriggerName(recipeId);
        Date time = new Date(System.currentTimeMillis() + projectConfig.getOptions().getTimeout() * Constants.MINUTE);

        Trigger timeoutTrigger = new SimpleTrigger(name, TIMEOUT_TRIGGER_GROUP, time);
        timeoutTrigger.setJobName(FatController.TIMEOUT_JOB_NAME);
        timeoutTrigger.setJobGroup(FatController.TIMEOUT_JOB_GROUP);
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

    private void checkNodeStatus(TreeNode<RecipeController> node)
    {
        RecipeController controller = node.getData();

        if (controller.isFinished())
        {
            controller.collect(buildResult, projectConfig.getOptions().getRetainWorkingCopy());
            executingControllers.remove(node);

            RecipeResult result = controller.getResult();
            if (result.succeeded())
            {
                initialiseNodes(controller.getChildBootstrapper(), node.getChildren());
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
        // If there is an SQL problem while saving the build result, the build becomes stuck and the server
        // needs to be restarted to clear it up.  To prevent the need for server restarts, we catch and log the exception
        // and continue.  This leaves the build result in an incorrect state, but will allow builds to continue. The
        // builds will be cleaned up next time the server restarts.  THIS IS ONLY A TEMPORARY FIX UNTIL WE WORK OUT
        // WHAT IS CAUSING THE SQL PROBLEMS (DEADLOCKS, STALE SESSIONS) IN THE FIRST PLACE.
        // Unfortunately, if we can not write to the db, then we are a little stuffed.
        try
        {
            buildResult.abortUnfinishedRecipes();
            buildResult.setHasWorkDir(projectConfig.getOptions().getRetainWorkingCopy());
            buildResult.complete();

            // The timing of this event is important: handlers of this event
            // are allowed to add information to and modify the state of the
            // build result.  Hence it is crucial that indexing and a final
            // save are done afterwards.
            eventManager.publish(new PostBuildEvent(this, buildResult));

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

            tree.cleanup(buildResult);
        }
        catch (Exception e)
        {
            LOG.severe("Failed to persist the completed build result. Reason: " + e.getMessage(), e);
        }

        eventManager.unregister(asyncListener);
        eventManager.publish(new BuildCompletedEvent(this, buildResult));
        asyncListener.stop(true);
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildCommencedEvent.class, RecipeEvent.class, BuildTerminationRequestEvent.class, RecipeTimeoutEvent.class};
    }

    public long getBuildId()
    {
        return buildResult.getId();
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

    public void setQueue(RecipeQueue queue)
    {
        this.queue = queue;
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

    public void setScmClientFactory(ScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }
}
