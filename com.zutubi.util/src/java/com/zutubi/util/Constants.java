package com.zutubi.util;

import java.util.Date;

/**
 * <class-comment/>
 */
public class Constants
{
    public static final long MEGABYTE = 1048576;

    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;
    public static final long WEEK = 7 * DAY;
    public static final long YEAR = 365 * DAY;

    public static final Date DAY_0 = new Date(0);

    // Line separator string.  This is the value of the line.separator
    // property at the moment that the StdOutErrReader was created.
    public static final String LINE_SEPARATOR = (String) System.getProperty("line.separator");
}
