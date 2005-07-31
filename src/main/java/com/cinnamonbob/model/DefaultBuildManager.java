package com.cinnamonbob.model;

import com.cinnamonbob.core2.BuildResult;
import com.cinnamonbob.model.persistence.BuildResultDao;

/**
 * 
 *
 */
public class DefaultBuildManager implements BuildManager
{
    private BuildResultDao BuildResultDao;

    public void setBuildResultDao(BuildResultDao dao)
    {
        BuildResultDao = dao;
    }
    
    public void save(BuildResult BuildResult)
    {
        BuildResultDao.save(BuildResult);
    }

    public BuildResult getBuildResult(long id)
    {
        return (BuildResult) BuildResultDao.findById(id);
    }
}
