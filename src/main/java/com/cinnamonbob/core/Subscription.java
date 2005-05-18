package com.cinnamonbob.core;

import java.util.LinkedList;
import java.util.List;

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
     * Conditions to be satisfied before notifying.
     */
    private List<NotifyCondition> conditions;
    
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
        this.conditions   = new LinkedList<NotifyCondition>();
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
    public boolean conditionsSatisfied(BuildResult result)
    {
        for(NotifyCondition condition: conditions)
        {
            if(!condition.satisfied(result))
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Adds the given condition to those that must be satisfied before the
     * contact point should be notified.
     * 
     * @param condition
     *        the condition to add
     */
    public void addCondition(NotifyCondition condition)
    {
        conditions.add(condition);
    }
}
