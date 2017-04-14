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

import com.zutubi.pulse.master.dependency.DependencyGraphData;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraph;
import com.zutubi.pulse.master.dependency.SimpleTreeLayoutAlgorithm;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.Point;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.adt.Grid;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.adt.TreeNode;

import java.util.List;

/**
 * Renders the trees in project dependency graphs to grid-based diagrams so
 * that they may be visualised.  Grids are used as they translate directly to
 * HTML tables.
 */
public class ProjectDependencyGraphRenderer
{
    /**
     * Factor for scaling from abstract x positions to grid x positions.
     * Accounts for the grid space used to draw connecting lines.
     */
    public static final int SCALE_FACTOR_X = 3;
    /**
     * Factor for scaling from abstract y positions to grid y positions.
     * Accounts for the fact that boxes are two cells high.
     */
    public static final int SCALE_FACTOR_Y = 2;
    
    private BuildManager buildManager;
    private Urls urls;

    public ProjectDependencyGraphRenderer(BuildManager buildManager, Urls urls)
    {
        this.buildManager = buildManager;
        this.urls = urls;
    }

    /**
     * Renders a tree of upstream dependencies.  This tree is rendered with the
     * leaves on the left flowing to the root on the right.
     *
     * @param graph graph to take the upstream tree from
     * @return a rendered version of the graph's upstream tree
     */
    public Grid<ProjectDependencyData> renderUpstream(ProjectDependencyGraph graph)
    {
        Grid<ProjectDependencyData> grid = render(graph.getUpstreamRoot());
        grid.flipHorizontal(new UnaryProcedure<ProjectDependencyData>()
        {
            public void run(ProjectDependencyData dependencyData)
            {
                dependencyData.flipHorizontal();
            }
        });

        return grid;
    }

    /**
     * Renders a tree of downstream dependencies.  This tree is rendered with
     * the root on the left flowing to the leaves on the right.
     *
     * @param graph graph to take the downstream tree from
     * @return a rendered version of the graph's downstream tree
     */
    public Grid<ProjectDependencyData> renderDownstream(ProjectDependencyGraph graph)
    {
        return render(graph.getDownstreamRoot());
    }

    private Grid<ProjectDependencyData> render(TreeNode<DependencyGraphData> root)
    {
        SimpleTreeLayoutAlgorithm<DependencyGraphData> layout = new SimpleTreeLayoutAlgorithm<DependencyGraphData>();
        TreeNode<Pair<DependencyGraphData, Point>> layTree = layout.layout(root);
        Point bounds = layout.getBounds(layTree);

        // The additional 1 for x and 2 for y allow for the fact that the grid
        // encompasses the boxs at the right and bottom of the diagram.
        Grid<ProjectDependencyData> grid = new Grid<ProjectDependencyData>(bounds.getX() * SCALE_FACTOR_X + 1, bounds.getY() * SCALE_FACTOR_Y + 2);
        renderToGrid(layTree, grid, true);
        return grid;
    }

    private void renderToGrid(TreeNode<Pair<DependencyGraphData, Point>> node, Grid<ProjectDependencyData> grid, boolean root)
    {
        Point position = getGridPosition(node);

        // Fill the cell at our position, and mark the one below dead.
        DependencyGraphData data = node.getData().first;
        grid.getCell(position).setData(ProjectDependencyData.makeBox(data, ProjectHealth.getHealth(buildManager, data.getProject()), urls.projectHome(data.getProject()), root));
        grid.getCell(position.down()).setData(ProjectDependencyData.makeDead());

        if (!node.isLeaf())
        {
            // Draw the edge coming from us and the vertical line (if required).
            List<TreeNode<Pair<DependencyGraphData,Point>>> children = node.getChildren();
            boolean multiChild = children.size() > 1;
            if (multiChild)
            {
                TreeNode<Pair<DependencyGraphData, Point>> firstChild = children.get(0);
                TreeNode<Pair<DependencyGraphData, Point>> lastChild = children.get(children.size() - 1);
                Point firstChildPosition = getGridPosition(firstChild);
                Point lastChildPosition = getGridPosition(lastChild);

                Point currentPosition = firstChildPosition.down().left().left();
                while (currentPosition.getY() <= lastChildPosition.getY())
                {
                    grid.getCell(currentPosition).setData(ProjectDependencyData.makeBordered(true, currentPosition.getY() == position.getY()));
                    currentPosition = currentPosition.down();
                }
            }
            else
            {
                Point rightPosition = position.right();
                grid.getCell(rightPosition).setData(ProjectDependencyData.makeBordered(false, true));
                grid.getCell(rightPosition.right()).setData(ProjectDependencyData.makeBordered(false, true));
            }

            // Draw children, including an edge leading in to each one.
            for (TreeNode<Pair<DependencyGraphData, Point>> child: node)
            {
                grid.getCell(getGridPosition(child).left()).setData(ProjectDependencyData.makeBordered(false, true));
                renderToGrid(child, grid, false);
            }
        }
    }

    private Point getGridPosition(TreeNode<Pair<DependencyGraphData, Point>> node)
    {
        return treeToGrid(node.getData().second);
    }

    private Point treeToGrid(Point point)
    {
        return new Point(point.getX() * SCALE_FACTOR_X, point.getY() * SCALE_FACTOR_Y);
    }
}
