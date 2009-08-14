package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraph;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.Grid;
import com.zutubi.util.GridCell;
import com.zutubi.util.Point;
import com.zutubi.util.TreeNode;
import com.zutubi.util.io.IOUtils;

import java.io.IOException;
import static java.util.Arrays.asList;

public class ProjectDependencyGraphRendererTest extends PulseTestCase
{
    private ProjectDependencyGraphRenderer renderer = new ProjectDependencyGraphRenderer();

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

    private void downstreamTest(TreeNode<Project> tree) throws IOException
    {
        ProjectDependencyGraph graph = new ProjectDependencyGraph(null, tree);
        Grid<ProjectDependencyData> grid = renderer.renderDownstream(graph);
        assertEquals(IOUtils.inputStreamToString(getInput("txt")), renderToAscii(grid));
    }

    private void upstreamTest(TreeNode<Project> tree) throws IOException
    {
        ProjectDependencyGraph graph = new ProjectDependencyGraph(tree, null);
        Grid<ProjectDependencyData> grid = renderer.renderUpstream(graph);
        assertEquals(IOUtils.inputStreamToString(getInput("txt")), renderToAscii(grid));
    }

    private TreeNode<Project> getSimpleTree()
    {
        return node("a",
                node("b"),
                node("c"));
    }

    private TreeNode<Project> getTrivialTree()
    {
        return node("x");
    }

    private TreeNode<Project> getComplexTree()
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
                    if (data.getProject() != null)
                    {
                        // Top half of a box
                        art.getCell(artCenter).setData(data.getProject().getName().charAt(0));
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

    private TreeNode<Project> node(String name, TreeNode<Project>... children)
    {
        Project project = new Project();
        project.setConfig(new ProjectConfiguration(name));
        TreeNode<Project> node = new TreeNode<Project>(project);
        node.addAll(asList(children));
        return node;
    }
}
