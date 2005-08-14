package com.cinnamonbob.model.persistence;

import java.util.List;

import com.cinnamonbob.model.BuildResult;

public interface BuildResultDao extends EntityDao<BuildResult>
{
    public List findLatestByProjectName(final String project, final int max);
    public BuildResult findByProjectNameAndNumber(final String project, final long number);
}
