package com.cinnamonbob.core;

/**
 * A subscription is a mapping from a project event to a contact point.  When
 * the event occurs, notifiaction is sent to the contact point.
 * 
 * @author jsankey
 */
public class Subscription
{
    /**
     * The type of event to notify on.
     */
    private Project.Event eventType;
    /**
     * The contact point to notify.
     */
    private ContactPoint contactPoint;
    
    //=======================================================================
    // Construction
    //=======================================================================

    /**
     * Constructs a new subscription connection the given event with the given
     * contact point.
     * 
     * @param eventType
     *        the type of event being subscribed to
     * @param contactPoint
     *        the contact point to notify on the event
     */
    public Subscription(Project.Event eventType, ContactPoint contactPoint)
    {
        this.eventType    = eventType;
        this.contactPoint = contactPoint;
    }

    /**
     * @return the contact point to notify
     */
    public ContactPoint getContactPoint()
    {
        return contactPoint;
    }
    
    /**
     * @return the type of event subscribed to
     */
    public Project.Event getEventType()
    {
        return eventType;
    }
    
    
}
