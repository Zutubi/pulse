package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.model.CommitMessageTransformer;

/**
 */
public class DeleteCommitMessageLinkAction extends CommitMessageTransformerActionSupport
{
    public String execute() throws Exception
    {
        CommitMessageTransformer transformer = getProjectManager().getCommitMessageTransformer(getId());
        if(transformer != null)
        {
            getProjectManager().delete(transformer);
        }
        
        return SUCCESS;
    }
}
