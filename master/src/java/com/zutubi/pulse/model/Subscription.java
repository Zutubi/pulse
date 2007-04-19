package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.util.logging.Logger;

/**
 * A subscription is a mapping from a project event to a contact point.  When
 * the event occurs, notification is sent to the contact point.
 *
 * @author jsankey
 */
public abstract class Subscription extends Entity
{
    private static final Logger LOG = Logger.getLogger(Subscription.class);

    /**
     * The contact point to notify.
     */
    private ContactPoint contactPoint;

    /**
     * The template to use to render this subscription.
     */
    private String template;

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
     * @param template name of the template to use to render builds for this
     *                 subscription
     */
    public Subscription(ContactPoint contactPoint, String template)
    {
        this.contactPoint = contactPoint;
        this.template = template;
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

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    /**
     * Indicates if the conditions for notifying the contact point are
     * satisfied by the given build model.
     *
     * @param result the build model to test the properties of
     * @return true iff the contact point should be notified for this model
     */
    public abstract boolean conditionSatisfied(BuildResult result);    
}
