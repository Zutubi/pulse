package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.Reference;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Table;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.prototype.config.user.contacts.ContactConfiguration;

/**
 * Abstract base for subscriptions.
 */
@SymbolicName("zutubi.subscriptionConfig")
@Table(columns = {"name"})
public abstract class SubscriptionConfiguration extends AbstractNamedConfiguration
{
    @Reference
    private ContactConfiguration contact;

    public ContactConfiguration getContact()
    {
        return contact;
    }

    public void setContact(ContactConfiguration contact)
    {
        this.contact = contact;
    }

    public abstract boolean conditionSatisfied(BuildResult buildResult);
    public abstract String getTemplate();
}
