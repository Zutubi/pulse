package com.zutubi.pulse.master.model;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.adt.DAGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A build dependency graph, linking a root build to all of its dependencies in one direction
 * (either upstream or downstream) transitively.  This is a DAG (Directed Acyclic Graph).
 */
public class BuildGraph extends DAGraph<BuildResult>
{
    /**
     * Creates a new graph rooted at the given node.
     *
     * @param root the root of the graph
     */
    public BuildGraph(Node<BuildResult> root)
    {
        super(root);
    }

    /**
     * Returns the node representing the given build, if any.
     * 
     * @param buildId id of the build to find the node for
     * @return the node for the given build, or null if there is no such node
     */
    public Node<BuildResult> findNodeByBuildId(final long buildId)
    {
        return findNodeByPredicate(new Predicate<DAGraph.Node<BuildResult>>()
        {
            public boolean satisfied(DAGraph.Node<BuildResult> node)
            {
                return node.getData().getId() == buildId;
            }
        });
    }

    /**
     * Returns all possible build paths for a given node.  Each path is a sequence of build results
     * found on nodes between the root (excluded) and the given node (included).
     * 
     * @param node the node to get the path for
     * @return all possible paths of build results from the root to the given node, empty if the
     *         node is not found in this graph (or is the root itself).
     */
    public Set<BuildPath> getBuildPaths(Node<BuildResult> node)
    {
        Set<List<Node<BuildResult>>> paths = getAllPathsTo(node);
        Set<BuildPath> buildPaths = new HashSet<BuildPath>();
        return CollectionUtils.map(paths, new Mapping<List<Node<BuildResult>>, BuildPath>()
        {
            public BuildPath map(List<Node<BuildResult>> path)
            {
                return new BuildPath(path);
            }
        }, buildPaths);
    }

    /**
     * Finds a node by a given build path, using the projects of the builds to walk the graph.  This
     * can be used to find equivalent nodes in two graphs rooted at builds of the same project.
     * Note that the shape of the graph can change between builds, so sometimes there is no
     * equivalent build to find.
     * 
     * @param buildPath build path from which the projects are extracted and used to traverse from
     *                  the root of this graph
     * @return the node found by traversing the full path, or null if no such node could be found
     */
    public Node<BuildResult> findNodeByProjects(BuildPath buildPath)
    {
        Node<BuildResult> node = getRoot();
        for (BuildResult build: buildPath)
        {
            node = nextByProjectId(node, build.getProject().getId());
            if (node == null)
            {
                break;
            }
        }
        
        return node;
    }

    private Node<BuildResult> nextByProjectId(Node<BuildResult> node, long projectId)
    {
        for (Node<BuildResult> connected: node.getConnected())
        {
            if (connected.getData().getProject().getId() == projectId)
            {
                return connected;
            }
        }

        return null;
    }

}
