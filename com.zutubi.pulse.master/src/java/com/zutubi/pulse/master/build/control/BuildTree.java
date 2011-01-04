package com.zutubi.pulse.master.build.control;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.TimeStamps;
import com.zutubi.util.TreeNode;
import com.zutubi.util.UnaryProcedure;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The build tree is a simple data structure used to hold the
 * recipe controllers during a build.
 *
 * The build tree provides a number of utility methods for
 * traversing the recipe controllers.
 */
public class BuildTree implements Iterable<RecipeController>
{
    private TreeNode<RecipeController> root;

    public BuildTree()
    {
        root = new TreeNode<RecipeController>(null);
    }

    public TreeNode<RecipeController> getRoot()
    {
        return root;
    }

    /**
     * Call {@link RecipeController#prepare(com.zutubi.pulse.master.model.BuildResult)} on
     * all of the recipe controllers.
     *
     * @param buildResult the build result passed as the argument to the prepare calls.
     */
    public void prepare(final BuildResult buildResult)
    {
        apply(new UnaryProcedure<TreeNode<RecipeController>>()
        {
            public void run(TreeNode<RecipeController> node)
            {
                node.getData().prepare(buildResult);
            }
        });
    }

    /**
     * Return the longest length of time (in milliseconds) remaining from the
     * recipe controllers.  
     *
     * @return a time in milliseconds that represents an estimate of the amount
     * of time remaining before the last controller will be finished.
     */
    public long getMaximumEstimatedTimeRemaining()
    {
        long longestRemaining = 0;
        for (RecipeController controller : this)
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

    public void apply(UnaryProcedure<TreeNode<RecipeController>> op)
    {
        apply(op, root);
    }

    private void apply(UnaryProcedure<TreeNode<RecipeController>> op, TreeNode<RecipeController> node)
    {
        for (TreeNode<RecipeController> child : node)
        {
            op.run(child);
            apply(op, child);
        }
    }

    public Iterator<RecipeController> iterator()
    {
        ControllerAccumulator accumulator = new ControllerAccumulator();
        apply(accumulator);
        return accumulator.getControllers().iterator();
    }

    private class ControllerAccumulator implements UnaryProcedure<TreeNode<RecipeController>>
    {
        private List<RecipeController> controllers = new LinkedList<RecipeController>();

        public List<RecipeController> getControllers()
        {
            return controllers;
        }

        public void run(TreeNode<RecipeController> node)
        {
            controllers.add(node.getData());
        }
    }
}
