package com.zutubi.pulse.committransformers;

import com.zutubi.pulse.model.persistence.CommitMessageTransformerDao;
import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.model.Project;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * <class comment/>
 */
public class DefaultCommitMessageTransformerManager implements CommitMessageTransformerManager
{
    private CommitMessageTransformerDao commitMessageTransformerDao;

    private Map<String, Class> transformerTypes = new HashMap<String, Class>();

    public DefaultCommitMessageTransformerManager()
    {
        transformerTypes.put("custom", CustomHandler.class);
        transformerTypes.put("jira", JiraHandler.class);
        transformerTypes.put("standard", StandardHandler.class);
    }

    public void save(CommitMessageTransformer entity)
    {
        commitMessageTransformerDao.save(entity);
    }

    public void delete(CommitMessageTransformer entity)
    {
        commitMessageTransformerDao.delete(entity);
    }

    public CommitMessageTransformer getById(long id)
    {
        return commitMessageTransformerDao.findById(id);
    }

    public List<CommitMessageTransformer> getCommitMessageTransformers()
    {
        return commitMessageTransformerDao.findAll();
    }

    public List<CommitMessageTransformer> getByProject(Project project)
    {
        return commitMessageTransformerDao.findByProject(project);
    }

    public CommitMessageTransformer getByName(String name)
    {
        return commitMessageTransformerDao.findByName(name);
    }

    public void setCommitMessageTransformerDao(CommitMessageTransformerDao commitMessageTransformerDao)
    {
        this.commitMessageTransformerDao = commitMessageTransformerDao;
    }

    public List<String> getTransformerTypes()
    {
        return new LinkedList<String>(transformerTypes.keySet());
    }

    public Class getTransformerHandler(String type)
    {
        return transformerTypes.get(type);
    }
}
