package com.cinnamonbob.model.persistence;

import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;

import java.util.List;

public interface BuildResultDao extends EntityDao<BuildResult>
{
    public List<BuildResult> findLatestByProject(final Project project, final int max);

    public BuildResult findByProjectAndNumber(final Project project, final long number);
}
