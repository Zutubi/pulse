package com.cinnamonbob;

import com.cinnamonbob.core.Bootstrapper;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.event.AsynchronousDelegatingListener;
import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.core.util.TreeNode;
import com.cinnamonbob.events.build.BuildCommencedEvent;
import com.cinnamonbob.events.build.BuildCompletedEvent;
import com.cinnamonbob.events.build.RecipeEvent;
import com.cinnamonbob.model.*;
import com.cinnamonbob.scm.SCMException;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class BuildController implements EventListener
{
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

    public BuildController(Project project, BuildSpecification specification, EventManager eventManager, BuildManager buildManager, RecipeQueue queue, RecipeResultCollector collector)
    {
        this.project = project;
        this.specification = specification;
        this.eventManager = eventManager;
        this.buildManager = buildManager;
        this.queue = queue;
        this.collector = collector;
        this.asyncListener = new AsynchronousDelegatingListener(this);
    }

    public void run()
    {
        createBuildTree();

        MasterBuildPaths paths = new MasterBuildPaths();
        File buildDir = paths.getBuildDir(project, buildResult);
        buildResult.commence(buildDir);
        buildManager.save(buildResult);

        // It is important that this directory is created *after* the build
        // result is commenced and saved to the database, so that the
        // database knows of the possibility of some other persistent
        // artifacts, even if an error occurs very early in the build.
        if (!buildDir.mkdirs())
        {
            throw new BuildException("Unable to create build directory '" + buildDir.getAbsolutePath() + "'");
        }

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
        buildResult = new BuildResult(project, buildManager.getNextBuildNumber(project));
        buildManager.save(buildResult);
        configure(root, buildResult.getRoot(), specification.getRoot());

        return tree;
    }

    private ScmBootstrapper createBuildBootstrapper()
    {
        ScmBootstrapper bootstrapper = new ScmBootstrapper();

        for (Scm scm : project.getScms())
        {
            try
            {
                Revision revision = scm.createServer().getLatestRevision();
                bootstrapper.add(new ScmCheckoutDetails(scm, revision));
            }
            catch (SCMException e)
            {
                throw new BuildException("Could not retrieve latest revision from SCM '" + scm.getName() + "'", e);
            }
        }

        return bootstrapper;
    }

    private void run(Bootstrapper bootstrapper, TreeNode<RecipeController> node)
    {
        executingControllers.add(node);
        node.getData().initialise(bootstrapper);
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
            RecipeRequest recipeRequest = new RecipeRequest(recipeResult.getId(), project.getBobFile(), stage.getRecipe());
            RecipeDispatchRequest dispatchRequest = new RecipeDispatchRequest(stage.getHostRequirements(), recipeRequest);
            RecipeController rc = new RecipeController(childResultNode, dispatchRequest, collector, queue, buildManager);
            TreeNode<RecipeController> child = new TreeNode<RecipeController>(rc);
            rcNode.add(child);
            configure(child, childResultNode, node);
        }
    }

    public void handleEvent(Event evt)
    {
        if (evt instanceof BuildCommencedEvent)
        {
            BuildCommencedEvent e = (BuildCommencedEvent) evt;
            if (e.getResult() == buildResult)
            {
                handleBuildCommenced();
            }
        }
        else
        {
            RecipeEvent e = (RecipeEvent) evt;
            handleRecipeEvent(e);
        }
    }

    private void handleBuildCommenced()
    {
        tree.prepare(buildResult);

        // execute the first level of recipe controllers...
        Bootstrapper initialBootstrapper = createBuildBootstrapper();
        for (TreeNode<RecipeController> node : tree.getRoot())
        {
            run(initialBootstrapper, node);
        }
    }

    private void handleRecipeEvent(RecipeEvent e)
    {
        List<TreeNode<RecipeController>> finishedNodes = new LinkedList<TreeNode<RecipeController>>();

        for (TreeNode<RecipeController> node : executingControllers)
        {
            RecipeController controller = node.getData();
            controller.handleRecipeEvent(e);
            if (controller.isFinished())
            {
                controller.collect(buildResult);
                finishedNodes.add(node);
                for (TreeNode<RecipeController> child : node.getChildren())
                {
                    run(controller.getChildBootstrapper(), child);
                }

            }
        }

        for (TreeNode<RecipeController> node : finishedNodes)
        {
            executingControllers.remove(node);
        }

        if (executingControllers.size() == 0)
        {
            completeBuild();
        }
    }

    private void completeBuild()
    {
        tree.cleanup(buildResult);
        buildResult.complete();
        buildManager.save(buildResult);
        eventManager.unregister(asyncListener);
        eventManager.publish(new BuildCompletedEvent(this, buildResult));
        asyncListener.stop();
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildCommencedEvent.class, RecipeEvent.class};
    }
}
