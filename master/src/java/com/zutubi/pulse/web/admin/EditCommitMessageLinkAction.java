package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.xwork.interceptor.Preparable;

import java.util.Arrays;
import java.util.List;

/**
 */
public class EditCommitMessageLinkAction extends CommitMessageTransformerActionSupport implements Preparable
{
    private CommitMessageTransformer transformer;

    public CommitMessageTransformer getTransformer()
    {
        return transformer;
    }

    public List<String> getPrepareParameterNames()
    {
        return Arrays.asList(new String[]{"id"});
    }

    public void prepare() throws Exception
    {
        transformer = getProjectManager().getCommitMessageTransformer(getId());
        if(transformer == null)
        {
            addActionError("Unknown link [" + getId() + "]");
        }
    }

    public String doInput() throws Exception
    {
        if(hasErrors())
        {
            return ERROR;
        }

        setSelectedProjects(transformer.getProjects());

        return INPUT;
    }

    public void validate()
    {
        if(hasErrors())
        {
            return;
        }

        CommitMessageTransformer t = getProjectManager().findCommitMessageTransformerByName(transformer.getName());
        if(t != null && t.getId() != getId())
        {
            addFieldError("transformer.name", "A commit message link with name '" + transformer.getName() + "' already exists");
        }
    }

    public String execute() throws Exception
    {
        List<Long> projects = transformer.getProjects();
        projects.clear();
        projects.addAll(getSelectedProjects());
        getProjectManager().save(transformer);
        return SUCCESS;
    }

}
