package com.cinnamonbob.model;

import com.cinnamonbob.core2.BuildResult;

/**
 * A subscription is a mapping from a project event to a contact point.  When
 * the event occurs, notifiaction is sent to the contact point.
 * 
 * @author jsankey
 */
public class Subscription
{
    /**
     * The contact point to notify.
     */
    private ContactPoint contactPoint;
    /**
     * Condition to be satisfied before notifying.
     */
    private NotifyCondition condition;
    
    //=======================================================================
    // Construction
    //=======================================================================

    /**
     * Constructs a new subscription connection the given event with the given
     * contact point.
     * 
     * @param contactPoint
     *        the contact point to notify on the event
     */
    public Subscription(ContactPoint contactPoint)
    {
        this.contactPoint = contactPoint;
        this.condition = new TrueNotifyCondition();
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
     * Indicates if the conditions for notifying the contact point are
     * satisfied by the given build result.
     * 
     * @param result
     *        the build result to test the properties of
     * @return true iff the contact point should be notified for this result
     */
    public boolean conditionSatisfied(BuildResult result)
    {
        return condition.satisfied(result);
    }
    
    /**
     * Sets the given condition as that which must be satisfied before the
     * contact point should be notified.
     * 
     * @param condition
     *        the condition to set
     */
    public void setCondition(NotifyCondition condition)
    {
        this.condition = condition;
    }
}
