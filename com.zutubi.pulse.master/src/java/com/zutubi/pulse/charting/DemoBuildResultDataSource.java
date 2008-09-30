package com.zutubi.pulse.charting;

import com.zutubi.pulse.model.BuildResult;

import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;

/**
 * <class comment/>
 */
public class DemoBuildResultDataSource implements BuildResultsDataSource
{
    private List<BuildResult> data;

    public DemoBuildResultDataSource(List<BuildResult> data)
    {
        this.data = data;
    }

    public BuildResultsResultSet getLastByBuilds(int builds)
    {
        if (builds >= data.size())
        {
            return new BuildResultsResultSet(data);
        }
        return new BuildResultsResultSet(data.subList(data.size() - builds, data.size() - 1));
    }

    public BuildResultsResultSet getLastByDays(int days)
    {
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_YEAR, -days);
        long starttime = start.getTimeInMillis();

        List<BuildResult> results = new LinkedList<BuildResult>();
        for (BuildResult result : data)
        {
            if (result.getStamps().getEndTime() > starttime)
            {
                results.add(result);
            }
        }
        return new BuildResultsResultSet(results);
    }
}
