package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.events.AsynchronousDelegatingListener;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.*;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.scheduling.quartz.TimeoutRecipeJob;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.TreeNode;
import com.zutubi.pulse.util.logging.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class BuildController implements EventListener
{
    private static final String TIMEOUT_TRIGGER_GROUP = "timeout";
    private static final Logger LOG = Logger.getLogger(BuildController.class);

    private BuildRevision revision;
    private BuildReason reason;
    private Project project;
    private BuildSpecification specification;
    private EventManager eventManager;
    private ProjectManager projectManager;
    private BuildManager buildManager;
    private MasterConfigurationManager configurationManager;
    private RecipeQueue queue;
    private RecipeResultCollector collector;
    private BuildTree tree;
    private BuildResult buildResult;
    private AsynchronousDelegatingListener asyncListener;
    private List<TreeNode<RecipeController>> executingControllers = new LinkedList<TreeNode<RecipeController>>();
    private Scheduler quartzScheduler;
    private ServiceTokenManager serviceTokenManager;

    public BuildController(BuildRequestEvent event, BuildSpecification specification, EventManager eventManager, ProjectManager projectManager, BuildManager buildManager, RecipeQueue queue, RecipeResultCollector collector, Scheduler quartScheduler, MasterConfigurationManager configManager, ServiceTokenManager serviceTokenManager)
    {
        this.revision = event.getRevision();
        this.reason = event.getReason();
        this.project = event.getProject();
        this.specification = specification;
        this.eventManager = eventManager;
        this.projectManager = projectManager;
        this.buildManager = buildManager;
        this.queue = queue;
        this.collector = collector;
        this.quartzScheduler = quartScheduler;
        this.asyncListener = new AsynchronousDelegatingListener(this);
        this.configurationManager = configManager;
        this.serviceTokenManager = serviceTokenManager;
    }

    public void run()
    {
        createBuildTree();

        // Fail early if things are not as expected.
        if (!buildResult.isPersistent())
            throw new RuntimeException("Build result must be a persistent instance.");

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File buildDir = paths.getBuildDir(project, buildResult);
        buildResult.setAbsoluteOutputDir(configurationManager.getDataDirectory(), buildDir);
        buildResult.queue();
        buildManager.save(buildResult);

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
        buildResult = new BuildResult(reason, project, specification.getName(), projectManager.getNextBuildNumber(project));
        buildManager.save(buildResult);
        configure(root, buildResult.getRoot(), specification, specification.getRoot());

        return tree;
    }

    private void configure(TreeNode<RecipeController> rcNode, RecipeResultNode resultNode, BuildSpecification specification, BuildSpecificationNode specNode)
    {
        for (BuildSpecificationNode node : specNode.getChildren())
        {
            BuildStage stage = node.getStage();
            RecipeResult recipeResult = new RecipeResult(stage.getRecipe());
            RecipeResultNode childResultNode = new RecipeResultNode(stage.getName(), recipeResult);
            resultNode.addChild(childResultNode);
            buildManager.save(resultNode);

            MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
            recipeResult.setAbsoluteOutputDir(configurationManager.getDataDirectory(), paths.getOutputDir(project, buildResult, recipeResult.getId()));

            RecipeRequest recipeRequest = new RecipeRequest(recipeResult.getId(), stage.getRecipe(), getResourceRequirements(specification, node));
            RecipeDispatchRequest dispatchRequest = new RecipeDispatchRequest(stage.getHostRequirements(), revision, recipeRequest, buildResult);
            RecipeController rc = new RecipeController(childResultNode, dispatchRequest, collector, queue, buildManager, serviceTokenManager);
            TreeNode<RecipeController> child = new TreeNode<RecipeController>(rc);
            rcNode.add(child);
            configure(child, childResultNode, specification, node);
        }
    }

    private List<ResourceRequirement> getResourceRequirements(BuildSpecification specification, BuildSpecificationNode node)
    {
        List<ResourceRequirement> requirements = new LinkedList<ResourceRequirement>();
        requirements.addAll(specification.getRoot().getResourceRequirements());
        requirements.addAll(node.getResourceRequirements());
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
                if (e.getResult() == buildResult)
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
                handleRecipeTimeout((RecipeTimeoutEvent)evt);
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

        // check project configuration to determine which bootstrap configuration should be used.
        Bootstrapper initialBootstrapper;
        boolean checkoutOnly = project.getCheckoutScheme() == Project.CheckoutScheme.CHECKOUT_ONLY;
        if (checkoutOnly)
        {
            initialBootstrapper = new CheckoutBootstrapper(project.getScm(), revision);
        }
        else
        {
            initialBootstrapper = new ProjectRepoBootstrapper(project.getName(), project.getScm(), revision);
        }
        PulseFileDetails pulseFileDetails = project.getPulseFileDetails();
        ComponentContext.autowire(pulseFileDetails);
        tree.prepare(buildResult);

        // execute the first level of recipe controllers...
        initialiseNodes(initialBootstrapper, tree.getRoot().getChildren());
    }

    private String getTriggerName(long recipeId)
    {
        return String.format("recipe-%d", recipeId);
    }

    private void handleBuildTerminationRequest(BuildTerminationRequestEvent event)
    {
        // Tell every running recipe to stop, and mark the build terminating
        // (so it will go into the error state on completion).
        for (TreeNode<RecipeController> controllerNode : executingControllers)
        {
            controllerNode.getData().terminateRecipe(event.isTimeout());
        }

        buildResult.terminate(event.isTimeout());
    }

    private void handleRecipeTimeout(RecipeTimeoutEvent event)
    {
        for (TreeNode<RecipeController> controllerNode : executingControllers)
        {
            RecipeController controller = controllerNode.getData();
            if(controller.getResult().getId() == event.getRecipeId())
            {
                controller.getResult().terminate(true);
                controller.terminateRecipe(true);
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
                if (!buildResult.commenced())
                {
                    handleFirstCommenced(foundNode.getData());
                }

                if (specification.getTimeout() != BuildSpecification.TIMEOUT_NEVER)
                {
                    scheduleTimeout(e.getRecipeId());
                }
            }
            else if (e instanceof RecipeCompletedEvent)
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

            checkNodeStatus(foundNode);
        }
    }

    /**
     * Called when the first recipe for this build is successfully
     * commenced.  It is at this point that the build is said to have
     * commenced.
     */
    private void handleFirstCommenced(RecipeController controller)
    {
        RecipeDispatchRequest dispatchRequest = controller.getDispatchRequest();
        RecipeRequest request = dispatchRequest.getRequest();

        // can retreive the revision from the dispatch request here

        try
        {
            FileSystemUtils.createFile(new File(buildResult.getAbsoluteOutputDir(configurationManager.getDataDirectory()), BuildResult.PULSE_FILE), request.getPulseFileSource());
        }
        catch(IOException e)
        {
            LOG.warning("Unable to save pulse file for build: " + e.getMessage(), e);
        }

        getChanges(dispatchRequest.getRevision());
        buildResult.commence(controller.getResult().getStamps().getStartTime());
        buildManager.save(buildResult);
    }

    private void getChanges(BuildRevision buildRevision)
    {
        Scm scm = project.getScm();
        Revision revision = buildRevision.getRevision();

        try
        {
            SCMServer server = scm.createServer();
            List<BuildResult> previousBuildResults = buildManager.getLatestCompletedBuildResults(project, specification.getName(), 1);

            if (previousBuildResults.size() == 1)
            {
                BuildScmDetails previousScmDetails = previousBuildResults.get(0).getScmDetails();
                if (previousScmDetails != null)
                {
                    Revision previousRevision = previousScmDetails.getRevision();
                    if (previousRevision != null)
                    {
                        getChangeSince(server, previousRevision, revision);
                    }
                }
            }
        }
        catch (SCMException e)
        {
            LOG.warning("Unable to retrieve changelist details from SCM server: " + e.getMessage(), e);
        }

        BuildScmDetails scmDetails = new BuildScmDetails(revision);
        buildResult.setScmDetails(scmDetails);
    }

    private List<Changelist> getChangeSince(SCMServer server, Revision previousRevision, Revision revision)
            throws SCMException
    {
        List<Changelist> result = new LinkedList<Changelist>();
        List<Changelist> scmChanges = server.getChanges(previousRevision, revision, "");

        // Get the uid after the changes as Svn requires a connection to be
        // made first
        String uid = server.getUid();

        for (Changelist change : scmChanges)
        {
            // Have we already got this revision?
            Changelist alreadySaved = buildManager.getChangelistByRevision(uid, change.getRevision());
            if(alreadySaved != null)
            {
                change = alreadySaved;
            }

            change.addProjectId(buildResult.getProject().getId());
            change.addResultId(buildResult.getId());
            buildManager.save(change);
            result.add(change);
        }

        return result;
    }

    private void scheduleTimeout(long recipeId)
    {
        String name = getTriggerName(recipeId);
        Date time = new Date(System.currentTimeMillis() + specification.getTimeout() * Constants.MINUTE);

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
            controller.collect(buildResult, specification.getRetainWorkingCopy());
            executingControllers.remove(node);

            RecipeResult result = controller.getResult();
            if (result.succeeded())
            {
                initialiseNodes(controller.getChildBootstrapper(), node.getChildren());
            }
            else if (result.failed())
            {
                buildResult.failure("Recipe " + result.getRecipeNameSafe() + " failed");
            }
            else if (result.errored())
            {
                buildResult.error("Error executing recipe " + result.getRecipeNameSafe());
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
        buildResult.abortUnfinishedRecipes();
        tree.cleanup(buildResult);
        buildResult.setHasWorkDir(specification.getRetainWorkingCopy());        
        buildResult.complete();

        for(PostBuildAction action: buildResult.getProject().getPostBuildActions())
        {
            action.execute(buildResult);
        }

        buildManager.save(buildResult);
        eventManager.unregister(asyncListener);
        eventManager.publish(new BuildCompletedEvent(this, buildResult));
        asyncListener.stop(true);
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildCommencedEvent.class, RecipeEvent.class, BuildTerminationRequestEvent.class};
    }

    public long getBuildId()
    {
        return buildResult.getId();
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
