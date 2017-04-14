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
     * @return true to process the node traversed to, false to ignore it and continue to the next
     *         sibling (note if false is returned no matching call to {@link #pop()} will be made)
     */
    boolean push(String edge);

    /**
     * Called to process a node.
     *
     * @param t node to process
     */
    void process(T t);

    /**
     * Called after a node is processed, when returning from the traversed
     * edge.
     */
    void pop();
}
