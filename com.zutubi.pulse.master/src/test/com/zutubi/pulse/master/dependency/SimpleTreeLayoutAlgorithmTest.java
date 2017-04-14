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

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.Point;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.adt.Grid;
import com.zutubi.util.adt.GridCell;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.adt.TreeNode;

import static java.util.Arrays.asList;

public class SimpleTreeLayoutAlgorithmTest extends PulseTestCase
{
    private SimpleTreeLayoutAlgorithm<String> algorithm = new SimpleTreeLayoutAlgorithm<String>();

    public void testLayoutSingle()
    {
        assertLayout("x\n", node("x"));
    }

    public void testLayoutOneChild()
    {
        assertLayout("pc\n", node("p", node("c")));
    }

    public void testLayoutLine()
    {
        assertLayout("pcg\n", node("p", node("c", node("g"))));
    }

    public void testBinary()
    {
        assertLayout(" 1\n" +
                     "x \n" +
                     " 2\n",
                node("x",
                        node("1"),
                        node("2")));
    }

    public void testBinaryTwoLevels()
    {
        assertLayout("  1\n" +
                     " 0 \n" +
                     "  2\n" +
                     "x  \n" +
                     "  b\n" +
                     " a \n" +
                     "  c\n",
                node("x",
                        node("0",
                                node("1"),
                                node("2")),
                        node("a",
                                node("b"),
                                node("c"))));
    }

    public void testThreeChildren()
    {
        assertLayout(" 1\n" +
                     "  \n" +
                     "x2\n" +
                     "  \n" +
                     " 3\n",
                node("x",
                        node("1"),
                        node("2"),
                        node("3")));
    }

    public void testOddAndEvenSubtrees()
    {
        assertLayout("  1\n" +
                     "   \n" +
                     " 02\n" +
                     "   \n" +
                     "x 3\n" +
                     "   \n" +
                     "  b\n" +
                     " a \n" +
                     "  c\n",
                node("x",
                        node("0",
                                node("1"),
                                node("2"),
                                node("3")),
                        node("a",
                                node("b"),
                                node("c"))));
    }

    public void testVaryingDepths()
    {
        assertLayout("     n\n" +
                     "      \n" +
                     " 0123o\n" +
                     "      \n" +
                     "     p\n" +
                     "x     \n" +
                     "  b   \n" +
                     "      \n" +
                     " ac   \n" +
                     "      \n" +
                     "  d   \n",
                node("x",
                        node("0",
                                node("1",
                                        node("2",
                                                node("3",
                                                        node("n"),
                                                        node("o"),
                                                        node("p"))))),
                        node("a",
                                node("b"),
                                node("c"),
                                node("d"))));
    }

    private void assertLayout(String expected, TreeNode<String> root)
    {
        TreeNode<Pair<String, Point>> layedOut = algorithm.layout(root);
        assertEquals(expected, drawLayout(layedOut));
    }

    private TreeNode<String> node(String data, TreeNode<String>... children)
    {
        TreeNode<String> node = new TreeNode<String>(data);
        node.addAll(asList(children));
        return node;
    }

    private String drawLayout(TreeNode<Pair<String, Point>> layedOut)
    {
        // Draw a layout using a grid of characters, so it can be consumed by
        // human eyeballs.
        Point bounds = algorithm.getBounds(layedOut);
        final Grid<String> grid = new Grid<String>(bounds.getX() + 1, bounds.getY() + 1);
        layedOut.depthFirstWalk(new UnaryProcedure<TreeNode<Pair<String, Point>>>()
        {
            public void run(TreeNode<Pair<String, Point>> node)
            {
                Pair<String, Point> nodeData = node.getData();
                grid.getCell(nodeData.second).setData(nodeData.first);
            }
        });

        StringBuilder result = new StringBuilder((bounds.getX() + 1) * bounds.getY());
        for (Iterable<GridCell<String>> rowsIt: grid)
        {
            for (GridCell<String> cell: rowsIt)
            {
                String cellData = cell.getData();
                result.append(cellData == null ? " " : cellData);
            }

            result.append("\n");
        }

        return result.toString();
    }
}
