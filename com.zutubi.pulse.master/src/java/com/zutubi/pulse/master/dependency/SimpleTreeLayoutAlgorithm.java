/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.dependency;

import com.google.common.base.Function;
import com.zutubi.util.Point;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.adt.TreeNode;

import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.zutubi.util.CollectionUtils.asPair;


/**
 * Implements a basic algorithm for laying out a tree in two-dimensional space
 * so that it can be visualised.  The layout begins with the root on the left
 * and leaves end up on the right.  Nodes are spread vertically to avoid
 * overlap in a simple and conservative fashion (i.e. more vertical space may
 * be used than is strictly required).  A resulting tree may look something
 * like:
 *
 * <pre>{@code
 *  b
 * a
 *    d
 *  c
 *    e
 * }</pre>
 *
 * if rendered in simple text.
 */
public class SimpleTreeLayoutAlgorithm<T>
{
    /**
     * Lays out the given tree in two dimensional space by assigning a point to
     * each node.  The returned tree contains nodes with the original node data
     * plus the nodes position as a point.
     *
     * @param root the root of the tree to lay out
     * @return a copy of the given tree with a point assigned to each node
     */
    public TreeNode<Pair<T, Point>> layout(TreeNode<T> root)
    {
        return layout(root, 0, new LayoutState());
    }

    /**
     * Given a laid-out tree, returns a point representing the bottom-right
     * corner of the area that the tree occupies.  That is, a point with the
     * largest x and largest y coordinates assigned to any nodes in the tree.
     *
     * @param root root of a layed-out tree to get the bounds for
     * @return a point representing the outer bounds of the tree
     */
    public Point getBounds(TreeNode<Pair<T, Point>> root)
    {
        final int maxX[] = new int[]{0};
        final int maxY[] = new int[]{0};

        root.depthFirstWalk(new UnaryProcedure<TreeNode<Pair<T, Point>>>()
        {
            public void run(TreeNode<Pair<T, Point>> node)
            {
                Point nodePosition = node.getData().second;
                if (nodePosition.getX() > maxX[0])
                {
                    maxX[0] = nodePosition.getX();
                }

                if (nodePosition.getY() > maxY[0])
                {
                    maxY[0] = nodePosition.getY();
                }
            }
        });

        return new Point(maxX[0], maxY[0]);
    }

    private TreeNode<Pair<T, Point>> layout(TreeNode<T> node, final int depth, final LayoutState state)
    {
        if (node.isLeaf())
        {
            return new TreeNode<Pair<T, Point>>(asPair(node.getData(), new Point(depth, state.allocateNextY())));
        }
        else
        {
            List<TreeNode<Pair<T,Point>>> children = newArrayList(transform(node, new Function<TreeNode<T>, TreeNode<Pair<T, Point>>>()
            {
                public TreeNode<Pair<T, Point>> apply(TreeNode<T> child)
                {
                    return layout(child, depth + 1, state);
                }
            }));

            TreeNode<Pair<T, Point>> result = new TreeNode<Pair<T, Point>>(asPair(node.getData(), new Point(depth, getAverageY(children))));
            result.addAll(children);
            return result;
        }
    }

    private int getAverageY(List<TreeNode<Pair<T, Point>>> nodes)
    {
        int total = 0;
        for (TreeNode<Pair<T, Point>> node: nodes)
        {
            total += node.getData().second.getY();
        }

        return total / nodes.size();
    }

    private static class LayoutState
    {
        private int nextY = 0;

        public int allocateNextY()
        {
            int result = nextY;
            nextY += 2;
            return result;
        }
    }
}
