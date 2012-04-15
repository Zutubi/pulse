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
