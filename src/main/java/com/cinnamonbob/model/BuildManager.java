package com.cinnamonbob.model;

import java.util.List;

/**
 * 
 *
 */
public interface BuildManager
{
    void save(BuildResult result);

    BuildResult getBuildResult(long id);

    List<BuildResult> getLatestBuildResultsForProject(String project, int max);

    BuildResult getByProjectNameAndNumber(final String project, final long number);

    StoredArtifact getArtifact(long id);
}
