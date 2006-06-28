package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.model.CommitMessageTransformer;

/**
 */
public class CreateCommitMessageLinkAction extends CommitMessageTransformerActionSupport
{
    private CommitMessageTransformer transformer = new CommitMessageTransformer();

    public CommitMessageTransformer getTransformer()
    {
        return transformer;
    }

    public void validate()
    {
        if(getProjectManager().findCommitMessageTransformerByName(transformer.getName()) != null)
        {
            addFieldError("transformer.name", "A commit message link with name '" + transformer.getName() + "' already exists");
        }
    }

    public String execute() throws Exception
    {
        transformer.setProjects(getSelectedProjects());
        getProjectManager().save(transformer);
        return SUCCESS;
    }
}
