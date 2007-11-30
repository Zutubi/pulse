package com.zutubi.pulse.model;

import com.zutubi.pulse.committransformers.CommitMessageBuilder;
import com.zutubi.pulse.core.model.Entity;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * A CommitMessageTransformer is used to search for a pattern in SCM commit
 * messages and replace it with a different string when shown in the UI.  The
 * primary use case is to link to external tools (e.g. link "bug 123" to the
 * web UI for the bug tracker).
 */
public abstract class CommitMessageTransformer extends Entity implements NamedEntity
{
    private String name;
    private List<Long> projects = new LinkedList<Long>();

    /**
     * This properties instance holds the custom configuration details for the handler.
     */
    private Properties properties;

    public CommitMessageTransformer()
    {
    }

    public CommitMessageTransformer(String name)
    {
        this.name = name;
    }
    
    public CommitMessageTransformer(String name, String a, String b)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Long> getProjects()
    {
        return projects;
    }

    public void setProjects(List<Long> projects)
    {
        this.projects = projects;
    }

    protected Properties getProperties()
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        return properties;
    }

    private void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    public boolean appliesToChangelist(Set<Long> changelistProjects)
    {
        if(projects == null || projects.size() == 0)
        {
            return true;
        }

        for(Long id: projects)
        {
            if(changelistProjects.contains(id))
            {
                return true;
            }
        }
        return false;
    }

    public abstract CommitMessageBuilder transform(CommitMessageBuilder builder);

    public abstract String getType();
}
