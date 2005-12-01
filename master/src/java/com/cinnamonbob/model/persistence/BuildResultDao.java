package com.cinnamonbob.model.persistence;

import com.cinnamonbob.model.BuildResult;

import java.util.List;

public interface BuildResultDao extends EntityDao<BuildResult>
{
    public List<BuildResult> findLatestByProjectName(final String project, final int max);

    public BuildResult findByProjectNameAndNumber(final String project, final long number);
}
