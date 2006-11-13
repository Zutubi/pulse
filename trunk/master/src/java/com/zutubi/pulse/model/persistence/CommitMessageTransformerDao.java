package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.model.Project;

import java.util.List;

/**
 */
public interface CommitMessageTransformerDao extends EntityDao<CommitMessageTransformer>
{
    List<CommitMessageTransformer> findByProject(Project project);

    CommitMessageTransformer findByName(String name);
}
