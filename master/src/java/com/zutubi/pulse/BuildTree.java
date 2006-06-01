package com.zutubi.pulse;

import com.zutubi.pulse.util.TreeNode;
import com.zutubi.pulse.util.TreeNodeOperation;
import com.zutubi.pulse.model.BuildResult;

/**
 */
public class BuildTree
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

    public void cleanup(final BuildResult buildResult)
    {
        apply(new TreeNodeOperation<RecipeController>()
        {

            public void apply(TreeNode<RecipeController> node)
            {
                node.getData().cleanup(buildResult);
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

}
