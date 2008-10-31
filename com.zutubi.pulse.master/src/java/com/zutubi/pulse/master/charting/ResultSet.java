package com.zutubi.pulse.master.charting;

/**
 * <class comment/>
 */
public interface ResultSet
{
    boolean next();

    Object getFieldValue(String fieldName);

    boolean hasNext();
}
