package com.cinnamonbob.model;

import java.util.List;

import com.cinnamonbob.core2.BuildResult;
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
    
    public BuildResult getByProjectNameAndNumber(final String project, final long number)
    {
        return buildResultDao.findByProjectNameAndNumber(project, number);
    }

    public StoredArtifact getArtifact(long id)
    {
        return artifactDao.findById(id);
    }

}
