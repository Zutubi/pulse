package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;

/**
 * A subscription is a mapping from a project event to a contact point.  When
 * the event occurs, notifiaction is sent to the contact point.
 *
 * @author jsankey
 */
public class Subscription extends Entity
{
    /**
     * The contact point to notify.
     */
    private ContactPoint contactPoint;

    /**
     * Condition to be satisfied before notifying.
     */
    private String condition;

    /**
     * The project to which this subscription is associated
     */
    private Project project;

    /**
     * A reference to the systems notify condition factory, used for instantiating
     * the subscriptions condition.
     */
    private NotifyConditionFactory notifyFactory;

    //=======================================================================
    // Construction
    //=======================================================================

    /**
     * Constructor to be used by hibernate only.
     */
    public Subscription()
    {

    }

    /**
     * Constructs a new subscription connection the given event with the given
     * contact point.
     *
     * @param contactPoint the contact point to notify on the event
     */
    public Subscription(Project project, ContactPoint contactPoint)
    {
        this.project = project;
        this.contactPoint = contactPoint;
        this.condition = NotifyConditionFactory.ALL_BUILDS;

        this.contactPoint.add(this);
    }

    //=======================================================================
    // Interface
    //=======================================================================

    /**
     * @return the contact point to notify
     */
    public ContactPoint getContactPoint()
    {
        return contactPoint;
    }

    /**
     * @param contactPoint
     */
    public void setContactPoint(ContactPoint contactPoint)
    {
        this.contactPoint = contactPoint;
    }

    public Project getProject()
    {
        return project;
    }

    private void setProject(Project project)
    {
        this.project = project;
    }

    /**
     * Sets the given condition as that which must be satisfied before the
     * contact point should be notified.
     *
     * @param condition the condition to set. See NotifyConditionFactory for valid condition
     * values.
     */
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public String getCondition()
    {
        return this.condition;
    }

    /**
     * Indicates if the conditions for notifying the contact point are
     * satisfied by the given build model.
     *
     * @param result the build model to test the properties of
     * @return true iff the contact point should be notified for this model
     */
    public boolean conditionSatisfied(BuildResult result)
    {
        NotifyCondition nc = notifyFactory.createCondition(condition);
        return nc.satisfied(result);
    }

    public void notify(BuildResult result)
    {
        getContactPoint().notify(result);
    }

    /**
     * The notify condition factory is a required resource, and used when checking
     * if the subscriptions 'nofitication condition' has been satisfied.
     *
     * @param notifyFactory
     */
    public void setNotifyConditionFactory(NotifyConditionFactory notifyFactory)
    {
        this.notifyFactory = notifyFactory;
    }
}
