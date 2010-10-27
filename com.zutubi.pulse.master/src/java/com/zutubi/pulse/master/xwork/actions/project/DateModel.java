package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.util.TimeStamps;

import java.util.Locale;

/**
 * Defines JSON data for a point in time in the past.
 */
public class DateModel
{
    private String absolute;
    private String relative;

    public DateModel(long time)
    {
        absolute = TimeStamps.getPrettyDate(time, Locale.getDefault());
        relative = TimeStamps.getPrettyTime(time);
    }

    public String getAbsolute()
    {
        return absolute;
    }

    public String getRelative()
    {
        return relative;
    }
}
