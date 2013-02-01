package com.zutubi.util.adt;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.zutubi.util.UnaryProcedure;

import java.util.*;

/**
 * A directed acyclic graph: a collection of interconnected nodes with directed connections and no cycles.
 */
public class DAGraph<T>
{
    private Node<T> root;

    /**
     * Creates a new graph rooted at the given node.
     *
     * @param root the root of the graph
     */
    public DAGraph(Node<T> root)
    {
        this.root = root;
    }

    /**
     * @return the root node for this graph
     */
    public Node<T> getRoot()
    {
        return root;
    }

    /**
     * Returns the first node satisfying the given predicate, if any.  Nodes are searched in
     * depth-first order.
     * 
     * @param predicate predicate used to identify a suitable node
     * @return the first node satisfying the given predicate, or null if there is no such node
     */
    public Node<T> findNodeByPredicate(Predicate<Node<T>> predicate)
    {
        return root.findByPredicate(predicate);
    }

    /**
     * Returns all possible paths from the root to a given node.  Each path is a sequence of nodes
     * found between the root (excluded) and the given node (included).
     * 
     * @param node the node to get the path for
     * @return all possible paths from the root to the given node, empty if the node is not found
     *         in this graph (or is the root itself).
     */
    public Set<List<Node<T>>> getAllPathsTo(Node<T> node)
    {
        return root.getPaths(node);
    }

    /**
     * Applies the given procedure to all nodes in this graph, starting at the root and working
     * downwards in depth-first fashion.
     * 
     * @param fn procedure to apply to each node
     */
    public void forEach(UnaryProcedure<Node<T>> fn)
    {
        root.forEach(fn, new HashSet<Node<T>>());
    }

    /**
     * Transforms this graph to a new graph of the same shape by mapping the data in all nodes
     * according to the given function.
     *
     * @param function mapping used to transform data
     * @param <U> new data type
     * @return a new graph shaped like this one with all data transformed
     */
    public <U> DAGraph<U> transform(Function<T, U> function)
    {
        return new DAGraph<U>(root.transform(function, new HashMap<Node<T>, Node<U>>()));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        @SuppressWarnings("unchecked")
        DAGraph<T> that = (DAGraph<T>) o;
        if (root != null ? !root.equals(that.root) : that.root != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return root != null ? root.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return root.toString();
    }

    /**
     * A single node in a graph, representing a specific datum.
     */
    public static class Node<T>
    {
        private T data;
        private List<Node<T>> connected = new LinkedList<Node<T>>();

        /**
         * Creates a new node with the given data.
         *
         * @param data data that the node represents
         */
        public Node(T data)
        {
            this.data = data;
        }

        /**
         * Adds a connection from this node to the given node.
         *
         * @param node the node to connect to.
         */
        public void connectNode(Node<T> node)
        {
            connected.add(node);
        }

        /**
         * @return the data represented by this node
         */
        public T getData()
        {
            return data;
        }

        /**
         * @return the set of all nodes reachable from this one.
         */
        public List<Node<T>> getConnected()
        {
            return Collections.unmodifiableList(connected);
        }

        Node<T> findByPredicate(Predicate<Node<T>> predicate)
        {
            if (predicate.apply(this))
            {
                return this;
            }
            else
            {
                for (Node<T> node: connected)
                {
                    Node<T> found = node.findByPredicate(predicate);
                    if (found != null)
                    {
                        return found;
                    }
                }

                return null;
            }
        }

        void forEach(UnaryProcedure<Node<T>> fn, Set<Node<T>> visited)
        {
            if (visited.contains(this))
            {
                return;
            }
            
            visited.add(this);
            fn.run(this);
            
            for (Node<T> node: connected)
            {
                node.forEach(fn, visited);
            }
        }

        Set<List<Node<T>>> getPaths(Node<T> node)
        {
            Set<List<Node<T>>> result = new HashSet<List<Node<T>>>();
            if (node == this)
            {
                result.add(Collections.<Node<T>>emptyList());
            }
            else
            {
                for (Node<T> n: connected)
                {
                    Set<List<Node<T>>> relativePaths = n.getPaths(node);
                    for (List<Node<T>> relativePath: relativePaths)
                    {
                        List<Node<T>> path = new LinkedList<Node<T>>();
                        path.add(n);
                        path.addAll(relativePath);
                        result.add(path);
                    }
                }
                
            }
            
            return result;
        }

        <U> Node<U> transform(Function<T, U> function, HashMap<Node<T>, Node<U>> mapped)
        {
            Node<U> transformed = mapped.get(this);
            if (transformed == null)
            {
                transformed = new Node<U>(function.apply(data));
                mapped.put(this, transformed);

                for (Node<T> child: connected)
                {
                    transformed.connectNode(child.transform(function, mapped));
                }
            }

            return transformed;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            @SuppressWarnings("unchecked")
            Node<T> node = (Node<T>) o;
            if (data != null ? !data.equals(node.data) : node.data != null)
            {
                return false;
            }
            if (connected != null ? !connected.equals(node.connected) : node.connected != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = data != null ? data.hashCode() : 0;
            result = 31 * result + (connected != null ? connected.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return data.toString() + " " + connected.toString();
        }

        /**
         * A predicate that finds a node with data equal to some fixed value.
         *
         * @param <T> type of the node data
         */
        public static class DataEqualsPredicate<T> implements Predicate<Node<T>>
        {
            private T data;

            public DataEqualsPredicate(T data)
            {
                this.data = data;
            }

            public boolean apply(Node<T> node)
            {
                return data.equals(node.getData());
            }
        }

        /**
         * Maps from a node to the node's data.
         *
         * @param <T> type of the node data
         */
        public static class ToDataFunction<T> implements Function<Node<T>, T>
        {
            public T apply(Node<T> node)
            {
                return node.getData();
            }
        }

        /**
         * Maps from data to a node representing said data.
         *
         * @param <T> type of the node data
         */
        public static class FromDataFunction<T> implements Function<T, Node<T>>
        {
            public Node<T> apply(T data)
            {
                return new Node<T>(data);
            }
        }

        /**
         * Comparator that sorts nodes by their data, using a comparator over the data type.
         * 
         * @param <T> type of the node data
         */
        public static class CompareByData<T> implements Comparator<Node<T>>
        {
            private Comparator<T> dataComparator;

            public CompareByData(Comparator<T> dataComparator)
            {
                this.dataComparator = dataComparator;
            }

            public int compare(Node<T> node1, Node<T> node2)
            {
                return dataComparator.compare(node1.getData(), node2.getData());
            }
        }
    }

    /**
     * Maps from a graph to that graph's root node.
     * 
     * @param <T> type of the graph data
     */
    public static class ToRootFunction<T> implements Function<DAGraph<T>, Node<T>>
    {
        public Node<T> apply(DAGraph<T> graph)
        {
            return graph.getRoot();
        }
    }
}
