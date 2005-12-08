package com.cinnamonbob.model;

import com.cinnamonbob.core.model.StoredArtifact;
import com.cinnamonbob.model.persistence.ArtifactDao;
import com.cinnamonbob.model.persistence.BuildResultDao;

import java.util.List;

/**
 * 
 *
 */
public class DefaultBuildManager implements BuildManager
{
    private BuildResultDao buildResultDao;
    private ArtifactDao artifactDao;

    public void setBuildResultDao(BuildResultDao dao)
    {
        buildResultDao = dao;
    }

    public void setArtifactDao(ArtifactDao dao)
    {
        artifactDao = dao;
    }

    public void save(BuildResult buildResult)
    {
        buildResultDao.save(buildResult);
    }

    public BuildResult getBuildResult(long id)
    {
        return buildResultDao.findById(id);
    }

    public List<BuildResult> getLatestBuildResultsForProject(Project project, int max)
    {
        return buildResultDao.findLatestByProject(project, max);
    }

    public BuildResult getLatestBuildResult(Project project)
    {
        List<BuildResult> results = getLatestBuildResultsForProject(project, 1);
        if (results.size() > 0)
        {
            return results.get(0);
        }
        return null;
    }

    public BuildResult getByProjectAndNumber(final Project project, final long number)
    {
        return buildResultDao.findByProjectAndNumber(project, number);
    }

    public StoredArtifact getArtifact(long id)
    {
        return artifactDao.findById(id);
    }

    public long getNextBuildNumber(Project project)
    {
        long number = 1;
        List<BuildResult> builds = getLatestBuildResultsForProject(project, 1);
        BuildResult previousBuildResult;

        if (builds.size() > 0)
        {
            previousBuildResult = builds.get(0);
            number = previousBuildResult.getNumber() + 1;
        }
        return number;
    }

}
