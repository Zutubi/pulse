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
     * Returns all new upstream changes in a build since a given build.  The upstream dependency
     * graphs of the build and since build are compared to find where new upstream builds have been
     * used.  For each new upstream build, the changes after the since upstream build are added to
     * the result.
     * 
     * @param build      the build to find upstream changes for
     * @param sinceBuild an earlier build of the same project used as the point after which to find
     *                   changes
     * @return all new upstream changes that may have influenced build since sinceBuild
     */
    List<UpstreamChangelist> getUpstreamChangelists(BuildResult build, BuildResult sinceBuild);
}
