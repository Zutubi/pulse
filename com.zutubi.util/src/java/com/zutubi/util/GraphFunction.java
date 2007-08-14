package com.zutubi.util;

/**
 * A simple function to process graph nodes, which is also given the context
 * of which edges are followed.
 */
public interface GraphFunction<T>
{
    /**
     * Called when an edge is traversed, before the node is processed.
     *
     * @param edge label of the edge traversed
     */
    void push(String edge);

    /**
     * Called to process a node.
     *
     * @param t node to process
     */
    void process(T t);

    /**
     * Called after a node is processed, when returning rfom the traversed
     * edge.
     */
    void pop();
}
