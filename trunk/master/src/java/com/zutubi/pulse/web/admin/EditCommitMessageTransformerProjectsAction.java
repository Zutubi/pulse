package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.xwork.interceptor.Preparable;

import java.util.Arrays;
import java.util.List;

/**
 * <class comment/>
 */
public class EditCommitMessageTransformerProjectsAction extends CommitMessageTransformerActionSupport implements Preparable
{
    private CommitMessageTransformer transformer;

    public void prepare() throws Exception
    {
        transformer = getTransformerManager().getById(getId());
        if(transformer == null)
        {
            addActionError("Unknown link [" + getId() + "]");
        }
    }

    public List<String> getPrepareParameterNames()
    {
        return Arrays.asList("id");
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

    public String execute() throws Exception
    {
        List<Long> projects = transformer.getProjects();
        projects.clear();
        projects.addAll(getSelectedProjects());
        getTransformerManager().save(transformer);

        return SUCCESS;
    }
}
