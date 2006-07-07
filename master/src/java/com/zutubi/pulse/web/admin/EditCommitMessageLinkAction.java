package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.xwork.interceptor.Preparable;

import java.util.Arrays;
import java.util.List;

/**
 */
public class EditCommitMessageLinkAction extends CommitMessageTransformerActionSupport implements Preparable
{
    private String newName;
    private CommitMessageTransformer transformer;

    public CommitMessageTransformer getTransformer()
    {
        return transformer;
    }

    public String getNewName()
    {
        return newName;
    }

    public void setNewName(String newName)
    {
        this.newName = newName;
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
        newName = transformer.getName();

        return INPUT;
    }

    public void validate()
    {
        if(hasErrors())
        {
            return;
        }

        CommitMessageTransformer t = getProjectManager().findCommitMessageTransformerByName(newName);
        if(t != null && t.getId() != getId())
        {
            addFieldError("newName", "A commit message link with name '" + newName + "' already exists");
        }
    }

    public String execute() throws Exception
    {
        transformer.setName(newName);
        List<Long> projects = transformer.getProjects();
        projects.clear();
        projects.addAll(getSelectedProjects());
        getProjectManager().save(transformer);
        return SUCCESS;
    }

}
