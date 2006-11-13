package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.committransformers.*;
import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.web.user.contact.FormAction;
import com.opensymphony.xwork.Validateable;

/**
 */
public class EditCommitMessageTransformerAction extends FormAction  implements Validateable
{
    private CommitMessageTransformerManager transformerManager;
    private ProjectManager projectManager;

    private long id;
    private long projectId;

    private String name;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public Project getProject()
    {
        return projectManager.getProject(projectId);
    }

    public void validate()
    {
        if(hasErrors())
        {
            return;
        }

        CommitMessageTransformer t = transformerManager.getByName(name);
        if(t != null && t.getId() != getId())
        {
            addFieldError("newName", "A commit message link with name '" + name + "' already exists");
        }
    }

    public Object doLoad()
    {
        CommitMessageTransformer transformer = transformerManager.getById(getId());
        if (transformer instanceof LinkCommitMessageTransformer)
        {
            LinkHandler handler = new LinkHandler();
            handler.setName(transformer.getName());
            handler.setExpression(((LinkCommitMessageTransformer)transformer).getExpression());
            handler.setLink(((LinkCommitMessageTransformer)transformer).getLink());
            return handler;
        }
        else if (transformer instanceof JiraCommitMessageTransformer)
        {
            JiraHandler handler = new JiraHandler();
            handler.setName(transformer.getName());
            handler.setUrl(((JiraCommitMessageTransformer)transformer).getUrl());
            return handler;
        }
        else if (transformer instanceof CustomCommitMessageTransformer)
        {
            CustomHandler handler = new CustomHandler();
            handler.setName(transformer.getName());
            handler.setExpression(((CustomCommitMessageTransformer)transformer).getExpression());
            handler.setReplacement(((CustomCommitMessageTransformer)transformer).getReplacement());
            return handler;
        }
        return null;
    }

    public void doSave(Object obj)
    {
        CommitMessageTransformer transformer = transformerManager.getById(getId());
        if (obj instanceof LinkHandler)
        {
            LinkHandler handler = (LinkHandler) obj;
            LinkCommitMessageTransformer standardTransformer = (LinkCommitMessageTransformer) transformer;
            standardTransformer.setName(handler.getName());
            standardTransformer.setExpression(handler.getExpression());
            standardTransformer.setLink(handler.getLink());

            transformerManager.save(standardTransformer);
        }
        else if (obj instanceof JiraHandler)
        {
            JiraHandler handler = (JiraHandler) obj;
            JiraCommitMessageTransformer jiraTransformer = (JiraCommitMessageTransformer) transformer;
            jiraTransformer.setName(handler.getName());
            jiraTransformer.setUrl(handler.getUrl());

            transformerManager.save(jiraTransformer);
        }
        else if (obj instanceof CustomHandler)
        {
            CustomHandler handler = (CustomHandler) obj;
            CustomCommitMessageTransformer customTransformer = (CustomCommitMessageTransformer) transformer;
            customTransformer.setName(handler.getName());
            customTransformer.setExpression(handler.getExpression());
            customTransformer.setReplacement(handler.getReplacement());

            transformerManager.save(customTransformer);
        }
    }

    public void setCommitMessageTransformerManager(CommitMessageTransformerManager commitMessageTransformerManager)
    {
        this.transformerManager = commitMessageTransformerManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
