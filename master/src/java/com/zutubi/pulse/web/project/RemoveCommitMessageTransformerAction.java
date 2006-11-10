package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.CommitMessageTransformer;

/**
 * <class comment/>
 */
public class RemoveCommitMessageTransformerAction extends ProjectActionSupport
{
    private long id;

    public void setId(long id)
    {
        this.id = id;
    }

    public void validate()
    {
        CommitMessageTransformer transformer = getTransformerManager().getById(id);
        if (transformer == null)
        {
            addActionError(String.format("Unknown transformer '%s'", id));
        }
    }

    public String execute() throws Exception
    {
        CommitMessageTransformer transformer = getTransformerManager().getById(id);
        transformer.getProjects().remove(projectId);
        getTransformerManager().save(transformer);

        return SUCCESS;
    }
}
