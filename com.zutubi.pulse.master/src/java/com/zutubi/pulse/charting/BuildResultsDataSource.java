package com.zutubi.pulse.charting;

/**
 * <class comment/>
 */
public interface BuildResultsDataSource
{
    BuildResultsResultSet getLastByBuilds(int builds);

    BuildResultsResultSet getLastByDays(int days);
}
