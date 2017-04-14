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

package com.zutubi.pulse.master.model;

import com.zutubi.util.adt.DAGraph;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * A thin veil over a list of build results, used to represent a path through a dependency graph.
 */
public class BuildPath implements Iterable<BuildResult>
{
    private List<BuildResult> builds;

    /**
     * Creates a new path with the given sequence of builds.
     *
     * @param builds builds that make up the path, in order
     */
    public BuildPath(BuildResult... builds)
    {
        this.builds = Arrays.asList(builds);
    }

    /**
     * Creates a new path from a path through a build graph, by extracting the build from each node.
     *
     * @param path a path of graph nodes to extract builds from
     */
    public BuildPath(List<DAGraph.Node<BuildResult>> path)
    {
        this.builds = newArrayList(transform(path, new DAGraph.Node.ToDataFunction<BuildResult>()));
    }

    /**
     * @return an immutable view of the builds in this path
     */
    public List<BuildResult> getBuilds()
    {
        return Collections.unmodifiableList(builds);
    }

    /**
     * @return the length of this path
     */
    public int size()
    {
        return builds.size();
    }

    /**
     * Retrieves a build by index.
     *
     * @param i the zero-based index of the build to retrieve
     * @return the build at index i (must be within [0, size())).
     */
    public BuildResult get(int i)
    {
        return builds.get(i);
    }

    /**
     * @return an immutable iterator over the builds in this path
     */
    public Iterator<BuildResult> iterator()
    {
        return getBuilds().iterator();
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

        BuildPath buildPath = (BuildPath) o;

        if (builds != null ? !builds.equals(buildPath.builds) : buildPath.builds != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return builds != null ? builds.hashCode() : 0;
    }
}
