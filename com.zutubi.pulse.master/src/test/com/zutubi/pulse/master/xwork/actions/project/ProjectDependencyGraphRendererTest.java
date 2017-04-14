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

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.dependency.DependencyGraphData;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraph;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.Point;
import com.zutubi.util.adt.Grid;
import com.zutubi.util.adt.GridCell;
import com.zutubi.util.adt.TreeNode;
import com.zutubi.util.io.FileSystemUtils;

import java.io.IOException;

import static com.zutubi.util.io.FileSystemUtils.normaliseNewlines;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;

public class ProjectDependencyGraphRendererTest extends PulseTestCase
{
    private ProjectDependencyGraphRenderer renderer = new ProjectDependencyGraphRenderer(mock(BuildManager.class), Urls.getBaselessInstance());

    public void testDownstream() throws IOException
    {
        downstreamTest(getSimpleTree());
    }

    public void testUpstream() throws IOException
    {
        upstreamTest(getSimpleTree());
    }

    public void testTrivialDownstream() throws IOException
    {
        downstreamTest(getTrivialTree());
    }

    public void testTrivialUpstream() throws IOException
    {
        upstreamTest(getTrivialTree());
    }

    public void testComplexDownstream() throws IOException
    {
        downstreamTest(getComplexTree());
    }

    public void testComplexUpstream() throws IOException
    {
        upstreamTest(getComplexTree());
    }

    private void downstreamTest(TreeNode<DependencyGraphData> tree) throws IOException
    {
        ProjectDependencyGraph graph = new ProjectDependencyGraph(null, tree);
        Grid<ProjectDependencyData> grid = renderer.renderDownstream(graph);
        assertEquals(normaliseNewlines(readInputFully("txt")), renderToAscii(grid));
    }

    private void upstreamTest(TreeNode<DependencyGraphData> tree) throws IOException
    {
        ProjectDependencyGraph graph = new ProjectDependencyGraph(tree, null);
        Grid<ProjectDependencyData> grid = renderer.renderUpstream(graph);
        assertEquals(normaliseNewlines(readInputFully("txt")), renderToAscii(grid));
    }

    private TreeNode<DependencyGraphData> getSimpleTree()
    {
        return node("a",
                node("b"),
                node("c"));
    }

    private TreeNode<DependencyGraphData> getTrivialTree()
    {
        return node("x");
    }

    private TreeNode<DependencyGraphData> getComplexTree()
    {
        return node("x",
                        node("0",
                                node("1"),
                                node("2"),
                                node("3")),
                        node("a",
                                node("b"),
                                node("c",
                                        node("d",
                                                node("e")))));
    }

    private String renderToAscii(Grid<ProjectDependencyData> grid)
    {
        // Each table cell consists of a 3x3 ascii square.  The corners are
        // unused.  The other edges are used for four borders, and the middle
        // for content.  Borders collapse: so the right border of one cell is
        // the same as the left border of the cell to its right.
        Grid<Character> art = new Grid<Character>(grid.getWidth() * 2 + 1, grid.getHeigth() * 2 + 1);
        int y = 0;
        for (Iterable<GridCell<ProjectDependencyData>> rowsIt: grid)
        {
            int x = 0;
            for (GridCell<ProjectDependencyData> cell: rowsIt)
            {
                Point artCenter = new Point(x * 2 + 1, y * 2 + 1);
                ProjectDependencyData data = cell.getData();
                if (data != null)
                {
                    if (data.getName() != null)
                    {
                        // Top half of a box
                        art.getCell(artCenter).setData(data.getName().charAt(0));
                    }
                    else if (data.isDead())
                    {
                        // Bottom half of a box.
                        art.getCell(artCenter).setData('.');
                        art.getCell(artCenter.up()).setData(' ');
                        art.getCell(artCenter.left()).setData('|');
                        art.getCell(artCenter.right()).setData('|');
                        art.getCell(artCenter.down()).setData('-');
                    }

                    if (data.hasLeftBorder())
                    {
                        art.getCell(artCenter.left()).setData('|');
                    }

                    if (data.hasRightBorder())
                    {
                        art.getCell(artCenter.right()).setData('|');
                    }

                    if (data.hasTopBorder())
                    {
                        art.getCell(artCenter.up()).setData('-');
                    }

                    if (data.hasBottomBorder())
                    {
                        art.getCell(artCenter.down()).setData('-');
                    }

                    if (data.isRoot())
                    {
                        art.getCell(artCenter.left()).setData('!');
                        art.getCell(artCenter.right()).setData('!');
                    }
                }

                x++;
            }

            y++;
        }

        StringBuilder result = new StringBuilder((art.getWidth() + 1) * art.getHeigth());
        for (Iterable<GridCell<Character>> rowsIt: art)
        {
            for (GridCell<Character> cell: rowsIt)
            {
                Character cellData = cell.getData();
                result.append(cellData == null ? ' ' : cellData);
            }

            result.append('\n');
        }

        // Strip trailing spaces from all lines.
        return result.toString().replaceAll(" +\n", "\n");
    }

    private TreeNode<DependencyGraphData> node(String name, TreeNode<DependencyGraphData>... children)
    {
        Project project = new Project();
        project.setConfig(new ProjectConfiguration(name));
        TreeNode<DependencyGraphData> node = new TreeNode<DependencyGraphData>(new DependencyGraphData(project));
        node.addAll(asList(children));
        return node;
    }
}
