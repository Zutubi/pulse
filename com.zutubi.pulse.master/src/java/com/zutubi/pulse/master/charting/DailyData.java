package com.zutubi.pulse.master.charting;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.Constants;

/**
 */
public class DailyData
{
    private long time;
    private int  successCount;
    private int  failureCount;
    private long totalBuildTime;
    private double totalStageTime;
    private long totalTests;

    public DailyData(long time)
    {
        this.time = time;
    }

    public void addBuild(BuildResultsResultSet set)
    {
        if(set.getState() == ResultState.SUCCESS)
        {
            successCount++;
            totalBuildTime += set.getElapsed() / Constants.SECOND;
            totalStageTime += set.getAverageStageTime() / Constants.SECOND;
            totalTests += set.getTotalTests();
        }
        else
        {
            failureCount++;
        }
    }

    public long getTime()
    {
        return time;
    }

    public int getSuccessCount()
    {
        return successCount;
    }

    public int getFailureCount()
    {
        return failureCount;
    }

    public int getTotalCount()
    {
        return successCount + failureCount;
    }

    public double getAverageBuildTime()
    {
        if(successCount > 0)
        {
            return ((double)totalBuildTime) / successCount;
        }
        else
        {
            return 0;
        }
    }

    public double getAverageStageTime()
    {
        if(successCount > 0)
        {
            return totalStageTime / successCount;
        }
        else
        {
            return 0;
        }
    }

    public double getAverageTestCount()
    {
        if(successCount > 0)
        {
            return ((double)totalTests) / successCount;
        }
        else
        {
            return 0;
        }
    }
}
