package com.zutubi.pulse.master.build.control;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.TreeNode;
import com.zutubi.util.UnaryProcedure;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
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

    public void prepare(final BuildResult buildResult)
    {
        apply(new UnaryProcedure<TreeNode<RecipeController>>()
        {
            public void process(TreeNode<RecipeController> node)
            {
                node.getData().prepare(buildResult);
            }
        });
    }

    public void apply(UnaryProcedure<TreeNode<RecipeController>> op)
    {
        apply(op, root);
    }

    private void apply(UnaryProcedure<TreeNode<RecipeController>> op, TreeNode<RecipeController> node)
    {
        for (TreeNode<RecipeController> child : node)
        {
            op.process(child);
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

        public void process(TreeNode<RecipeController> node)
        {
            controllers.add(node.getData());
        }
    }
}
