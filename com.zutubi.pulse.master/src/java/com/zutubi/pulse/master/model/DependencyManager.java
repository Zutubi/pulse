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

import com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor;

import java.util.List;

/**
 * A manager that deals with dependency relationships between builds.  We use Ivy to handle all of
 * the artifact publishing and retrieval.  This manager layers on top the smarts we need to
 * integrate dependency information into the Pulse logic and interface.
 */
public interface DependencyManager
{
    /**
     * Records links from the given build to all upstream dependencies as defined in the build's
     * module descriptor.  This should be done for each build as it completes to build up an index
     * of dependency information.
     *
     * @param build               the build to add upstream links for, must be complete
     * @param ivyModuleDescriptor the resolved module descriptor for the build
     */
    void addDependencyLinks(BuildResult build, IvyModuleDescriptor ivyModuleDescriptor);

    /**
     * Loads dependency information for the given build.  The information is derived for each stage
     * from the Ivy retrieval report for that stage (if found).
     *
     * @param build the build to load information for, must be complete
     * @return dependency information for each stage in the given build
     */
    List<StageRetrievedArtifacts> loadRetrievedArtifacts(BuildResult build);

    /**
     * Gets the graph of all builds that are upstream of a given build dependency-wise.  This is
     * all builds that the given build depended upon (transitively).  The root of the graph is the
     * given build.
     *
     * @param build build to get the upstream graph for
     * @return all builds that are upstream of the given build, graphed by dependency links
     */
    BuildGraph getUpstreamDependencyGraph(BuildResult build);

    /**
     * Gets the graph of all builds that are downstream of a given build dependency-wise.  This is
     * all builds that depended upon the given build (transitively).  The root of the graph is the
     * given build.
     *
     * @param build build to get the downstream graph for
     * @return all builds that are downstream of the given build, graphed by dependency links
     */
    BuildGraph getDownstreamDependencyGraph(BuildResult build);
}
