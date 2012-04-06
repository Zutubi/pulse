package com.zutubi.pulse.master.model;

import java.util.List;

/**
 * A manager that deals with dependency relationships between builds.  We use Ivy to handle all of
 * the artifact publishing and retrieval.  This manager layers on top the smarts we need to
 * integrate dependency information into the Pulse logic and interface.
 */
public interface DependencyManager
{
    /**
     * Records links from the given build to all upstream dependencies - that is builds that it
     * retrieved artifacts from.  This should be done for each build as it completes to build up
     * an index of dependency information.
     *
     * @param build the build to add upstream links for, must be complete
     */
    void addDependencyLinks(BuildResult build);

    /**
     * Loads dependency information for the given build.  The information is derived for each stage
     * from the Ivy retrieval report for that stage (if found).
     *
     * @param build the build to load information for, must be complete
     * @return dependency information for each stage in the given build
     */
    List<StageRetrievedArtifacts> loadRetrievedArtifacts(BuildResult build);
}
