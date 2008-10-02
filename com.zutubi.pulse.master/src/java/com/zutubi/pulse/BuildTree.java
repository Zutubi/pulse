package com.zutubi.pulse;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.TreeNode;
import com.zutubi.util.TreeNodeOperation;

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
        apply(new TreeNodeOperation<RecipeController>()
        {
            public void apply(TreeNode<RecipeController> node)
            {
                node.getData().prepare(buildResult);
            }
        });
    }

    public void apply(TreeNodeOperation<RecipeController> op)
    {
        apply(op, root);
    }

    private void apply(TreeNodeOperation<RecipeController> op, TreeNode<RecipeController> node)
    {
        for (TreeNode<RecipeController> child : node)
        {
            op.apply(child);
            apply(op, child);
        }
    }

    public Iterator<RecipeController> iterator()
    {
        ControllerAccumulator accumulator = new ControllerAccumulator();
        apply(accumulator);
        return accumulator.getControllers().iterator();
    }

    private class ControllerAccumulator implements TreeNodeOperation<RecipeController>
    {
        private List<RecipeController> controllers = new LinkedList<RecipeController>();

        public List<RecipeController> getControllers()
        {
            return controllers;
        }

        public void apply(TreeNode<RecipeController> node)
        {
            controllers.add(node.getData());
        }
    }
}
