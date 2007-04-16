package com.zutubi.prototype.config;

import com.zutubi.prototype.config.events.ConfigurationEvent;

/**
 * An event listener that listens for configuration events.  This is the
 * preferred way to be notified of configuration changes, as it is a
 * simplification of the more general event handling mechanism.  Where more
 * flexibility is required, implement
 * {@link com.zutubi.pulse.events.EventListener}.
 */
public interface ConfigurationEventListener
{
    public void handleEvent(ConfigurationEvent event);
}
