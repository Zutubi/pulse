package com.zutubi.prototype.type.record;

/**
 * Convenient abstract base for record implementations.
 */
public abstract class AbstractRecord implements Record
{
    protected static final String ID_KEY = "id";
    protected static final long UNDEFINED = 0;

    public long getID()
    {
        String idString = getMeta(ID_KEY);
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
