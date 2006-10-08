package com.zutubi.pulse.charting;

/**
 * <class comment/>
 */
public interface ResultSet
{
    boolean next();

    Object getFieldValue(String fieldName);
}
