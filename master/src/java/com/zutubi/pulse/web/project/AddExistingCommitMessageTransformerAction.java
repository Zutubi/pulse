package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.CommitMessageTransformer;

/**
 * <class comment/>
 */
public class AddExistingCommitMessageTransformerAction extends ProjectActionSupport
{
    private String existing;

    public void setExisting(String existing)
    {
        this.existing = existing;
    }

    public void validate()
    {
        if (getTransformerManager().getByName(existing) == null)
        {
            addFieldError("existing", String.format("Unknown commit message transformer '%s'", existing));            
        }

        if (getProject() == null)
        {
            addActionError("Unknown project.");
        }
    }

    public String execute() throws Exception
    {
        CommitMessageTransformer transformer = getTransformerManager().getByName(existing);
        transformer.getProjects().add(getProjectId());
        getTransformerManager().save(transformer);

        return SUCCESS;
    }
}
