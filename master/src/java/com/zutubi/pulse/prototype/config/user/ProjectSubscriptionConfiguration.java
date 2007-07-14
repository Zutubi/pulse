package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Reference;
import com.zutubi.config.annotations.Select;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;

import java.util.List;

/**
 * A subscription to results for project builds.
 */
@SymbolicName("zutubi.projectSubscriptionConfig")
@Form(fieldOrder = {"name", "projects", "contact", "template"})
public class ProjectSubscriptionConfiguration extends SubscriptionConfiguration
{
    @Reference
    private List<ProjectConfiguration> projects;
    private SubscriptionConditionConfiguration condition;
    @Select(optionProvider = "SubscriptionTemplateOptionProvider")
    private String template;

    public List<ProjectConfiguration> getProjects()
    {
        return projects;
    }

    public void setProjects(List<ProjectConfiguration> projects)
    {
        this.projects = projects;
    }

    public SubscriptionConditionConfiguration getCondition()
    {
        return condition;
    }

    public void setCondition(SubscriptionConditionConfiguration condition)
    {
        this.condition = condition;
    }

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }
}
