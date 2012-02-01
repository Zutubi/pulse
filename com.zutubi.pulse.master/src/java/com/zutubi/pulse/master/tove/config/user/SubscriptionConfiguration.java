package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfiguration;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

/**
 * Abstract base for subscriptions.
 */
@SymbolicName("zutubi.subscriptionConfig")
@Table(columns = {"name"})
@Classification(collection = "favourites")
public abstract class SubscriptionConfiguration extends AbstractNamedConfiguration
{
    @Required
    @Reference
    private ContactConfiguration contact;
    @Select(optionProvider = "SubscriptionTemplateOptionProvider")
    private String template;
    @ControllingCheckbox(checkedFields = "logLineLimit")
    private boolean attachLogs;
    @Numeric(min = 0, max = 250)
    private int logLineLimit = 50;

    public ContactConfiguration getContact()
    {
        return contact;
    }

    public void setContact(ContactConfiguration contact)
    {
        this.contact = contact;
    }

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    public boolean isAttachLogs()
    {
        return attachLogs;
    }

    public void setAttachLogs(boolean attachLogs)
    {
        this.attachLogs = attachLogs;
    }

    public int getLogLineLimit()
    {
        return logLineLimit;
    }

    public void setLogLineLimit(int logLineLimit)
    {
        this.logLineLimit = logLineLimit;
    }

    public abstract boolean conditionSatisfied(BuildResult buildResult);
}
