package com.cinnamonbob.slave;

import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.events.build.RecipeEvent;
import com.cinnamonbob.services.MasterService;
import com.cinnamonbob.util.logging.Logger;

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
                LOG.severe("Could not forward event for recipe '" + Long.toString(id) + "' to master", e);
            }
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{RecipeEvent.class};
    }
}
