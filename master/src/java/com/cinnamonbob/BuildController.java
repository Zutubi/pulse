package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.core.Bootstrapper;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.core.util.Constants;
import com.cinnamonbob.core.util.TreeNode;
import com.cinnamonbob.events.AsynchronousDelegatingListener;
import com.cinnamonbob.events.Event;
import com.cinnamonbob.events.EventListener;
import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.events.build.*;
import com.cinnamonbob.model.*;
import com.cinnamonbob.scheduling.quartz.TimeoutBuildJob;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;
import com.cinnamonbob.util.logging.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.io.File;
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

    private Project project;
    private BuildSpecification specification;
    private EventManager eventManager;
    private BuildManager buildManager;
    private RecipeQueue queue;
    private RecipeResultCollector collector;
    private BuildTree tree;
    private BuildResult buildResult;
    private AsynchronousDelegatingListener asyncListener;
    private List<TreeNode<RecipeController>> executingControllers = new LinkedList<TreeNode<RecipeController>>();
    private Scheduler quartzScheduler;

    public BuildController(Project project, BuildSpecification specification, EventManager eventManager, BuildManager buildManager, RecipeQueue queue, RecipeResultCollector collector, Scheduler quartScheduler)
    {
        this.project = project;
        this.specification = specification;
        this.eventManager = eventManager;
        this.buildManager = buildManager;
        this.queue = queue;
        this.collector = collector;
        this.quartzScheduler = quartScheduler;
        this.asyncListener = new AsynchronousDelegatingListener(this);
    }

    public void run()
    {
        createBuildTree();

        MasterBuildPaths paths = new MasterBuildPaths();
        File buildDir = paths.getBuildDir(project, buildResult);
        buildResult.queue(buildDir);
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
        buildResult = new BuildResult(project, specification.getName(), buildManager.getNextBuildNumber(project));
        buildManager.save(buildResult);
        configure(root, buildResult.getRoot(), specification.getRoot());

        return tree;
    }

    private ScmBootstrapper createBuildBootstrapper()
    {
        Scm scm = project.getScm();
        ScmBootstrapper bootstrapper = new ScmBootstrapper(scm);

        try
        {
            SCMServer server = scm.createServer();
            Revision latestRevision = server.getLatestRevision();
            bootstrapper.setRevision(latestRevision);

            // collect scm changes to be added to the build results.
            List<Changelist> scmChanges = null;

            try
            {
                List<BuildResult> previousBuildResults = buildManager.getLatestCompletedBuildResults(project, specification.getName(), 1);

                if (previousBuildResults.size() == 1)
                {
                    BuildScmDetails previousScmDetails = previousBuildResults.get(0).getScmDetails();
                    if (previousScmDetails != null)
                    {
                        Revision previousRevision = previousScmDetails.getRevision();
                        if (previousRevision != null)
                        {
                            scmChanges = server.getChanges(previousRevision, latestRevision, "");
                            for (Changelist change : scmChanges)
                            {
                                change.setResultId(buildResult.getId());
                            }
                        }
                    }
                }
            }
            catch (SCMException e)
            {
                LOG.warning("Unable to retrieve changelist details from SCM server: " + e.getMessage(), e);
            }

            BuildScmDetails scmDetails = new BuildScmDetails(latestRevision, scmChanges);
            buildResult.setScmDetails(scmDetails);
        }
        catch (SCMException e)
        {
            throw new BuildException("Could not retrieve latest revision from SCM: " + e.getMessage(), e);
        }

        buildManager.save(buildResult);

        return bootstrapper;
    }

    private void configure(TreeNode<RecipeController> rcNode, RecipeResultNode resultNode, BuildSpecificationNode specNode)
    {
        for (BuildSpecificationNode node : specNode.getChildren())
        {
            BuildStage stage = node.getStage();
            RecipeResult recipeResult = new RecipeResult(stage.getRecipe());
            RecipeResultNode childResultNode = new RecipeResultNode(recipeResult);
            resultNode.addChild(childResultNode);
            buildManager.save(resultNode);

            MasterBuildPaths paths = new MasterBuildPaths();
            recipeResult.setOutputDir(paths.getOutputDir(project, buildResult, recipeResult.getId()).getAbsolutePath());

            RecipeRequest recipeRequest = new RecipeRequest(recipeResult.getId(), stage.getRecipe());
            RecipeDispatchRequest dispatchRequest = new RecipeDispatchRequest(stage.getHostRequirements(), recipeRequest, buildResult);
            RecipeController rc = new RecipeController(childResultNode, dispatchRequest, collector, queue, buildManager);
            TreeNode<RecipeController> child = new TreeNode<RecipeController>(rc);
            rcNode.add(child);
            configure(child, childResultNode, node);
        }
    }

    public void handleEvent(Event evt)
    {
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
            else
            {
                RecipeEvent e = (RecipeEvent) evt;
                handleRecipeEvent(e);
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
        File buildDir = new File(buildResult.getOutputDir());
        if (!buildDir.mkdirs())
        {
            throw new BuildException("Unable to create build directory '" + buildDir.getAbsolutePath() + "'");
        }

        ScmBootstrapper initialBootstrapper = createBuildBootstrapper();
        BobFileDetails bobFileDetails = project.getBobFileDetails();
        ComponentContext.autowire(bobFileDetails);
        String bobFileSource = bobFileDetails.getBobFile(project, initialBootstrapper.getRevision());

        tree.prepare(buildResult, bobFileSource);

        // execute the first level of recipe controllers...
        initialiseNodes(initialBootstrapper, tree.getRoot().getChildren());

        if (specification.getTimeout() != BuildSpecification.TIMEOUT_NEVER)
        {
            scheduleTimeout();
        }
    }

    private void scheduleTimeout()
    {
        String name = getTriggerName();
        Date time = new Date(System.currentTimeMillis() + specification.getTimeout() * Constants.MINUTE);

        Trigger timeoutTrigger = new SimpleTrigger(name, TIMEOUT_TRIGGER_GROUP, time);
        timeoutTrigger.setJobName(FatController.TIMEOUT_JOB_NAME);
        timeoutTrigger.setJobGroup(FatController.TIMEOUT_JOB_GROUP);
        timeoutTrigger.getJobDataMap().put(TimeoutBuildJob.PARAM_ID, buildResult.getId());

        try
        {
            quartzScheduler.scheduleJob(timeoutTrigger);
        }
        catch (SchedulerException e)
        {
            LOG.severe("Unable to schedule build timeout trigger: " + e.getMessage(), e);
        }
    }

    private String getTriggerName()
    {
        return String.format("build-%d", buildResult.getId());
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
        if (e instanceof RecipeDispatchedEvent)
        {
            buildResult.recipeDispatched();
            buildManager.save(buildResult);
        }

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
            checkNodeStatus(foundNode);
        }
    }

    private void checkNodeStatus(TreeNode<RecipeController> node)
    {
        RecipeController controller = node.getData();

        if (controller.isFinished())
        {
            controller.collect(buildResult);
            executingControllers.remove(node);

            if (controller.succeeded())
            {
                initialiseNodes(controller.getChildBootstrapper(), node.getChildren());
            }
            else
            {
                buildResult.failure();
                buildManager.save(buildResult);
            }
        }

        if (executingControllers.size() == 0)
        {
            completeBuild();
        }
    }

    private void completeBuild()
    {
        try
        {
            quartzScheduler.unscheduleJob(getTriggerName(), TIMEOUT_TRIGGER_GROUP);
        }
        catch (SchedulerException e)
        {
            LOG.warning("Unable to unschedule timeout trigger: " + e.getMessage(), e);
        }

        buildResult.abortUnfinishedRecipes();
        tree.cleanup(buildResult);
        buildResult.complete();
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
}
