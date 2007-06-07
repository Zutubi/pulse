package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.user.contacts.ContactConfiguration;

import java.util.List;

/**
 *
 *
 */
@SymbolicName("internal.userSubscriptionConfig")
@Table(columns = {"name"})
@Form(fieldOrder = {"name", "projects", "contact", "template"})
public class SubscriptionConfiguration extends AbstractNamedConfiguration
{
    // contact point reference.
    @Reference
    private ContactConfiguration contact;

    @Reference
    private List<ProjectConfiguration> projects;

    private SubscriptionConditionConfiguration condition;

    @Select(optionProvider = "SubscriptionTemplateOptionProvider")
    private String template;

    public ContactConfiguration getContact()
    {
        return contact;
    }

    public void setContact(ContactConfiguration contact)
    {
        this.contact = contact;
    }

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
