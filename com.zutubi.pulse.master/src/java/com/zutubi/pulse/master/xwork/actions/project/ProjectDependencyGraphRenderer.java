package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.dependency.ProjectDependencyGraph;
import com.zutubi.pulse.master.dependency.SimpleTreeLayoutAlgorithm;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.*;

import java.util.List;

/**
 * Renders the trees in project dependency graphs to grid-based diagrams so
 * that they may be visualised.  Grids are used as the translate directly to
 * HTML tables.
 */
public class ProjectDependencyGraphRenderer
{
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
            public void process(ProjectDependencyData dependencyData)
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

    private Grid<ProjectDependencyData> render(TreeNode<Project> root)
    {
        SimpleTreeLayoutAlgorithm<Project> layout = new SimpleTreeLayoutAlgorithm<Project>();
        TreeNode<Pair<Project, Point>> layTree = layout.layout(root);
        Point bounds = layout.getBounds(layTree);

        final Grid<ProjectDependencyData> grid = new Grid<ProjectDependencyData>(bounds.getX() * 3 + 1, bounds.getY() * 2 + 2);
        layTree.depthFirstWalk(new TreeNodeOperation<Pair<Project, Point>>()
        {
            public void apply(TreeNode<Pair<Project, Point>> pairTreeNode)
            {
                renderToGrid(pairTreeNode, grid);
            }
        });

        return grid;
    }

    private void renderToGrid(TreeNode<Pair<Project, Point>> node, Grid<ProjectDependencyData> grid)
    {
        Point position = getGridPosition(node);

        // Fill the cell at our position, and mark the one below dead.
        grid.getCell(position).setData(ProjectDependencyData.makeBox(node.getData().first));
        grid.getCell(position.down()).setData(ProjectDependencyData.makeDead());

        if (!node.isLeaf())
        {
            // Draw the edge coming from us and the vertical line (if required).
            List<TreeNode<Pair<Project,Point>>> children = node.getChildren();
            boolean multiChild = children.size() > 1;
            if (multiChild)
            {
                TreeNode<Pair<Project, Point>> firstChild = children.get(0);
                TreeNode<Pair<Project, Point>> lastChild = children.get(children.size() - 1);
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
            for (TreeNode<Pair<Project, Point>> child: node)
            {
                grid.getCell(getGridPosition(child).left()).setData(ProjectDependencyData.makeBordered(false, true));
                renderToGrid(child, grid);
            }
        }
    }

    private Point getGridPosition(TreeNode<Pair<Project, Point>> node)
    {
        return treeToGrid(node.getData().second);
    }

    private Point treeToGrid(Point point)
    {
        return new Point(point.getX() * 3, point.getY() * 2);
    }
}
