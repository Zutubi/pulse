package com.zutubi.pulse.model;

/**
 * A subscription to a user's own personal builds.
 */
public class PersonalBuildSubscription extends Subscription
{
    public PersonalBuildSubscription()
    {
        super();
    }

    public PersonalBuildSubscription(ContactPoint contactPoint, String template)
    {
        super(contactPoint, template);
    }

    public boolean conditionSatisfied(BuildResult result)
    {
        return result.isPersonal() && result.getUser().equals(getContactPoint().getUser());
    }

    public boolean isPersonal()
    {
        return true;
    }
}
