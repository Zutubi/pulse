package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.util.Predicate;
import com.zutubi.util.UnaryFunction;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.UnaryProcedure;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple modelling of the template hierarchy in a scope to use during
 * upgrades.  This is deliberately independent of the tove code, as upgrade
 * logic needs to be frozen in time.
 */
public class ScopeHierarchy
{
    private Node root;

    /**
     * @param root the root node of the hierarchy, presumed to already be
     *             fleshed out
     */
    public ScopeHierarchy(Node root)
    {
         this.root = root;
    }

    public Node getRoot()
    {
        return root;
    }

    /**
     * Finds and returns the node with the given id, if any.
     *
     * @param id the id to search for
     * @return the node with the given id, or null if not found
     */
    public Node findNodeById(final String id)
    {
        return findNodeByPredicate(new Predicate<Node>()
        {
            public boolean satisfied(Node node)
            {
                return node.getId().equals(id);
            }
        });
    }

    /**
     * Finds and returns the first node to match the given predicate, if any.
     * The search order is not defined.
     *
     * @param predicate the predicate used to test nodes
     * @return the first node found to satisfy the predicate, or null if no
     *         nodes do
     */
    public Node findNodeByPredicate(Predicate<Node> predicate)
    {
        return root.findByPredicate(predicate);
    }

    /**
     * Runs a function over all nodes in the hierarchy.  If the function
     * returns false at a node, the subtree under the node will not be
     * processed.
     *
     * @param f the function to run
     */
    public void forEach(UnaryFunction<Node, Boolean> f)
    {
        root.forEach(f);
    }

    /**
     * Represents a single node in a template hierarchy.  One node will be
     * created for each record in the templated collection.
     */
    public static class Node
    {
        private String id;
        private Node parent;
        private List<Node> children = new LinkedList<Node>();

        /**
         * @param id the identifier of the node, which is the key of the
         *           associated record in the templated collection (the second
         *           element of the record's path).
         */
        public Node(String id)
        {
            this.id = id;
        }

        /**
         * Add a new child to this node, also wiring up its parent pointer.
         *
         * @param child the child to add
         */
        void addChild(Node child)
        {
            child.parent = this;
            children.add(child);
        }

        public String getId()
        {
            return id;
        }

        /**
         * @return our parent node, or null if we are the root
         */
        public Node getParent()
        {
            return parent;
        }

        /**
         * @return all of our child nodes, in no particular order
         */
        public List<Node> getChildren()
        {
            return Collections.unmodifiableList(children);
        }

        /**
         * @return true if this node has children, false otherwise.
         */
        public boolean hasChildren()
        {
            return children.size() > 0;
        }

        /**
         * Finds and returns the first node in our subtree to satisfy the given
         * predicate, if any.  This node is included in the search.  The search
         * order is undefined.
         *
         * @param predicate predicate used to test nodes
         * @return the first node in our subtree found to satisfy the
         *         predicate, or null if there is no such node
         */
        Node findByPredicate(Predicate<Node> predicate)
        {
            if (predicate.satisfied(this))
            {
                return this;
            }

            Node found = null;
            for (Node child: children)
            {
                found = child.findByPredicate(predicate);
                if (found != null)
                {
                    break;
                }
            }

            return found;
        }

        /**
         * Runs a function over this, and if the function returns true runs it
         * over every child node (recursively).  Essentially the function is
         * applied to the whole tree but stops traversing a branch when it
         * returns false.
         *
         * @param f the function to run
         */
        void forEach(UnaryFunction<Node, Boolean> f)
        {
            if (f.process(this))
            {
                for (Node child: children)
                {
                    child.forEach(f);
                }
            }
        }
    }
}
