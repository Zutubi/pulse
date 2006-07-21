package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.model.CommitMessageTransformer;

/**
 */
public class CreateCommitMessageLinkAction extends CommitMessageTransformerActionSupport
{
    private CommitMessageTransformer transformer = new CommitMessageTransformer();
    /**
     * Only used for redirecting back to the project configuration tab (if
     * that is where the user came from).
     */
    private long projectId = 0;

    public CommitMessageTransformer getTransformer()
    {
        return transformer;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
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

    public String redirect()
    {
        if(projectId == 0)
        {
            return "settings";
        }
        else
        {
            return "project";
        }
    }
}
