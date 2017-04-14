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

package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.master.model.BuildDependencyLink;

import java.util.List;

/**
 * Low-level persistence API for {@link BuildDependencyLink} entities.
 */
public interface BuildDependencyLinkDao extends EntityDao<BuildDependencyLink>
{
    /**
     * Returns all links that reference the given build (either upstream or downstream).
     *
     * @param buildId id of the build to get dependencies for
     * @return links for all dependencies involving the given build
     */
    List<BuildDependencyLink> findAllDependencies(long buildId);

    /**
     * Returns all links to upstream dependencies for the given build.
     *
     * @param buildId id of the build to get upstream dependencies for
     * @return links for all upstream dependencies of the given build
     */
    List<BuildDependencyLink> findAllUpstreamDependencies(long buildId);

    /**
     * Returns all links to downstream dependencies for the given build.
     *
     * @param buildId id of the build to get downstream dependencies for
     * @return links for all downstream dependencies of the given build
     */
    List<BuildDependencyLink> findAllDownstreamDependencies(long buildId);

    /**
     * Deletes all dependency links that reference the given build.
     *
     * @param buildId id of the build to delete links for
     * @return the number of links deleted
     */
    int deleteDependenciesByBuild(long buildId);
}
