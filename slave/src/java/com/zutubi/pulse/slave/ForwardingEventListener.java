/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.slave;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.build.RecipeEvent;
import com.zutubi.pulse.services.MasterService;
import com.zutubi.pulse.util.logging.Logger;

/**
 */
public class ForwardingEventListener implements EventListener
{
    private static final Logger LOG = Logger.getLogger(ForwardingEventListener.class);

    private MasterService service;
    private long id;

    public ForwardingEventListener(MasterService service, long id)
    {
        this.service = service;
        this.id = id;
    }

    public void handleEvent(Event event)
    {
        RecipeEvent recipeEvent = (RecipeEvent) event;
        if (recipeEvent.getRecipeId() == id)
        {
            try
            {
                event.setSource(null);
                service.handleEvent(event);
            }
            catch (Exception e)
            {
                // TODO abort the recipe execution
                // TODO support retrying events
                LOG.severe("Could not forward event for recipe '" + Long.toString(id) + "' to master", e);
            }
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{RecipeEvent.class};
    }
}
