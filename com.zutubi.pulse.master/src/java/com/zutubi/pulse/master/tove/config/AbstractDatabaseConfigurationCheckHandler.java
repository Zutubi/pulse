package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.logging.Logger;

/**
 * Helper base for configuration check handlers that test database connections.
 */
public abstract class AbstractDatabaseConfigurationCheckHandler<T extends Configuration>  extends AbstractConfigurationCheckHandler<T>
{
    private static final Logger LOG = Logger.getLogger(AbstractDatabaseConfigurationCheckHandler.class);
    private static final String MYSQL_INSANITY = "** BEGIN NESTED EXCEPTION **";

    protected void processException(Exception e) throws PulseException
    {
        LOG.warning(e);
        String message = e.getMessage();
        int i = message.indexOf(MYSQL_INSANITY);
        if(i >= 0)
        {
            message = message.substring(0, i).trim();
        }

        Throwable t = e;
        while ((t = t.getCause()) != null)
        {
            message += " Caused by: " + t.getMessage();
        }

        throw new PulseException(message, e);
    }
}
