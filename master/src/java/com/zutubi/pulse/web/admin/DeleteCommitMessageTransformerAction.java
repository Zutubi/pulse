package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.model.CommitMessageTransformer;

/**
 */
public class DeleteCommitMessageTransformerAction extends CommitMessageTransformerActionSupport
{
    public String execute() throws Exception
    {
        CommitMessageTransformer transformer = getTransformerManager().getById(getId());
        if(transformer != null)
        {
            getTransformerManager().delete(transformer);
        }
        
        return SUCCESS;
    }
}
