package com.zutubi.pulse.charting.demo;

import com.zutubi.pulse.charting.BuildResultsDataSource;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.BuildResult;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

/**
 * <class comment/>
 */
public class DemoDataSourceFactory
{
    private static final Random RAND = new Random();

    public static BuildResultsDataSource createBuildResultsDataSource()
    {
        return new BuildResultsDataSource(createBuildResults());
    }

    private static LinkedList<BuildResult> createBuildResults()
    {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_YEAR, -35);

        LinkedList<BuildResult> buildResults = new LinkedList<BuildResult>();
        int l = 0;
        for (int i = 0; i < 35; i++)
        {
            int j = RAND.nextInt(50);
            for (int k = 0; k < j; k++)
            {
                boolean success = RAND.nextInt(50) < 40;
                long finished = date.getTimeInMillis();
                long started = finished - 100 - (RAND.nextInt(30) + 2 * i + k);
                buildResults.add(createBuildResult(l++, success, started, finished));
            }
            date.add(Calendar.DAY_OF_YEAR, 1);
        }
        return buildResults;
    }

    private static BuildResult createBuildResult(long id, boolean successful, long started, long finished)
    {
        BuildResult result = new BuildResult();
        result.setId(id);
        if (successful)
        {
            result.setState(ResultState.SUCCESS);
        }
        else
        {
            result.setState(ResultState.FAILURE);
        }
        result.getStamps().setStartTime(started);
        result.getStamps().setEndTime(finished);
        return result;
    }


}
