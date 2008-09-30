package com.zutubi.pulse.tove.config.user;

import com.zutubi.config.annotations.Classification;
import com.zutubi.config.annotations.Reference;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Table;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.tove.config.user.contacts.ContactConfiguration;
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
