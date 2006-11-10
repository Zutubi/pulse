package com.zutubi.pulse.committransformers;

import com.zutubi.pulse.model.EntityManager;
import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.model.Project;

import java.util.List;

/**
 * <class comment/>
 */
public interface CommitMessageTransformerManager extends EntityManager<CommitMessageTransformer>
{
    CommitMessageTransformer getById(long id);

    List<CommitMessageTransformer> getCommitMessageTransformers();

    List<CommitMessageTransformer> getByProject(Project project);

    CommitMessageTransformer getByName(String name);

    List<String> getTransformerTypes();

    Class getTransformerHandler(String type);
}
