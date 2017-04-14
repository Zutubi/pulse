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

package com.zutubi.util.adt;

import com.google.common.base.Predicate;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.junit.ZutubiTestCase;
import static java.util.Arrays.asList;

import java.util.LinkedList;
import java.util.List;

public class TreeNodeTest extends ZutubiTestCase
{
    private TreeNode<String> root;

    @Override
    protected void setUp() throws Exception
    {
        root = new TreeNode<String>("0",
                new TreeNode<String>("1",
                        new TreeNode<String>("1-1"),
                        new TreeNode<String>("1-2",
                                new TreeNode<String>("1-2-1"))),
                new TreeNode<String>("2",
                        new TreeNode<String>("2-1"),
                        new TreeNode<String>("2-2"),
                        new TreeNode<String>("2-3")));
    }

    public void testClear()
    {
        assertEquals(2, root.getChildren().size());
        root.clear();
        assertEquals(0, root.getChildren().size());
    }

    public void testDepth()
    {
        assertEquals(3, root.depth());
        assertEquals(0, root.getChildren().get(0).getChildren().get(0).depth());
    }

    public void testDepthFirstWalk()
    {
        final List<String> order = new LinkedList<String>();
        root.depthFirstWalk(new UnaryProcedure<TreeNode<String>>()
        {
            public void run(TreeNode<String> node)
            {
                order.add(node.getData());
            }
        });

        assertEquals(asList("1-1", "1-2-1", "1-2", "1", "2-1", "2-2", "2-3", "2", "0"), order);
    }

    public void testBreadthFirstWalk()
    {
        assertEquals(asList("0", "1", "2", "1-1", "1-2", "2-1", "2-2", "2-3", "1-2-1"), breadthFirstWalk());
    }

    public void testFilteringWalk()
    {
        root.filteringWalk(new Predicate<TreeNode<String>>()
        {
            public boolean apply(TreeNode<String> node)
            {
                return !node.getData().contains("2");
            }
        });

        assertEquals(asList("0", "1", "1-1"), breadthFirstWalk());
    }

    private List<String> breadthFirstWalk()
    {
        final List<String> order = new LinkedList<String>();
        root.breadthFirstWalk(new UnaryProcedure<TreeNode<String>>()
        {
            public void run(TreeNode<String> node)
            {
                order.add(node.getData());
            }
        });
        return order;
    }

    public void testParents()
    {
        TreeNode<String> node = root.getChildren().get(0).getChildren().get(1).getChildren().get(0);
        assertEquals("1-2-1", node.getData());
        assertEquals("1-2", node.getParent().getData());
        assertEquals("1", node.getParent().getParent().getData());
        assertEquals("0", node.getParent().getParent().getParent().getData());
        assertNull(root.getParent());
    }
}
