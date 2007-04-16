package com.zutubi.prototype.config;

import com.zutubi.prototype.config.events.ConfigurationEvent;

/**
 * An event listener that listens for configuration events.  This is the
 * preferred way to be notified of configuration changes, but is suitable
 * only when tight control is not required.  ConfigurationEventListeners are
 * notified asynchronously.  When synchronous notification is required,
 * implement {@link com.zutubi.prototype.config.ConfigurationListener}.
 */
public interface ConfigurationEventListener
{
    public void handleEvent(ConfigurationEvent event);
}
