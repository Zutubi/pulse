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

package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.util.bean.ObjectFactory;

/**
 * A simple factory class providing convenience methods for creating
 * graph filter instances.
 */
public class GraphFilters
{
    private ObjectFactory objectFactory;

    /**
     * Create a new status filter instance.
     *
     * @param status    the status used as the focus on the filter.
     *
     * @return a new filter instance.
     *
     * @see com.zutubi.pulse.master.build.queue.graph.StatusFilter
     */
    public GraphFilter status(String status)
    {
        return objectFactory.buildBean(StatusFilter.class, status);
    }

    /**
     * Create a new transitive filter instance.
     *
     * @return a new filter instance.
     *
     * @see com.zutubi.pulse.master.build.queue.graph.TransitiveFilter
     */
    public GraphFilter transitive()
    {
        return objectFactory.buildBean(TransitiveFilter.class);
    }

    /**
     * Create a new trigger filter instance.
     *
     * @return a new filter instance.
     *
     * @see com.zutubi.pulse.master.build.queue.graph.TriggerFilter
     */
    public GraphFilter trigger()
    {
        return objectFactory.buildBean(TriggerFilter.class);
    }

    /**
     * Create a new duplicate filter instance.
     *
     * @return a new filter instance.
     *
     * @see com.zutubi.pulse.master.build.queue.graph.DuplicateFilter
     */
    public GraphFilter duplicate()
    {
        return objectFactory.buildBean(DuplicateFilter.class);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
