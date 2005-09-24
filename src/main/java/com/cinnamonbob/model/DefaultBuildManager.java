package com.cinnamonbob.model;

import java.util.List;

import com.cinnamonbob.model.persistence.ArtifactDao;
import com.cinnamonbob.model.persistence.BuildResultDao;

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
    
    public void save(BuildResult BuildResult)
    {
        buildResultDao.save(BuildResult);
    }

    public BuildResult getBuildResult(long id)
    {
        return buildResultDao.findById(id);
    }

    public List<BuildResult> getLatestBuildResultsForProject(String project, int max)
    {
        return buildResultDao.findLatestByProjectName(project, max);
    }

    public BuildResult getLatestBuildResult(String project)
    {
        List<BuildResult> results = getLatestBuildResultsForProject(project, 1);
        if (results.size() > 0)
        {
            return results.get(0);
        }
        return null;
    }

    public BuildResult getByProjectNameAndNumber(final String project, final long number)
    {
        return buildResultDao.findByProjectNameAndNumber(project, number);
    }

    public StoredArtifact getArtifact(long id)
    {
        return artifactDao.findById(id);
    }

    public long getNextBuildNumber(String projectName)
    {
        long              number = 1;
        List<BuildResult> builds = getLatestBuildResultsForProject(projectName, 1);
        BuildResult previousBuildResult;
        if(builds.size() > 0)
        {
            previousBuildResult = builds.get(0);
            number = previousBuildResult.getNumber() + 1;
        }
        return number;
    }

}
