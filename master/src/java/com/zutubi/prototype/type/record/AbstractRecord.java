package com.zutubi.prototype.type.record;

import java.util.Collection;

/**
 * Convenient abstract base for record implementations.
 */
public abstract class AbstractRecord implements Record
{
    protected static final String HANDLE_KEY = "handle";
    protected static final long UNDEFINED = 0;

    public long getHandle()
    {
        String idString = getMeta(HANDLE_KEY);
        if (idString != null)
        {
            try
            {
                return Long.parseLong(idString);
            }
            catch (NumberFormatException e)
            {
                // Illegal
            }
        }

        return UNDEFINED;
    }
}
