package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;
import com.cinnamonbob.core.util.Constants;
import com.cinnamonbob.core.util.TimeStamps;

/**
 * Stores the policy for periodic cleanup of build results for a project.
 * Each day results are checked against this policy
 */
public class AgeBuildResultCleanupPolicy extends Entity implements BuildResultCleanupPolicy
{
    /**
     * Value to specify when the result should never be cleaned.
     */
    public static int NEVER_CLEAN = 0;

    /**
     * The number of days to keep working directories around.
     */
    private long workDirDays;
    /**
     * The number of days to keep the full build details around.
     */
    private long resultDays;

    /**
     * For hibernate.
     */
    public AgeBuildResultCleanupPolicy()
    {
    }

    public AgeBuildResultCleanupPolicy(int workDirDays, int resultDays)
    {
        this.workDirDays = workDirDays;
        this.resultDays = resultDays;
    }

    public boolean canCleanupWorkDir(BuildResult result)
    {
        return workDirDays != NEVER_CLEAN && getResultAge(result) >= workDirDays;
    }

    public boolean canCleanupResult(BuildResult result)
    {
        return resultDays != NEVER_CLEAN && getResultAge(result) >= resultDays;
    }

    private long getResultAge(BuildResult result)
    {
        TimeStamps stamps = result.getStamps();
        long elapsed = System.currentTimeMillis() - stamps.getStartTime();
        return elapsed / Constants.DAY;
    }

    public long getWorkDirDays()
    {
        return workDirDays;
    }

    public void setWorkDirDays(long workDirDays)
    {
        this.workDirDays = workDirDays;
    }

    public String getPrettyWorkDirDays()
    {
        return getPrettyDays(workDirDays);
    }

    public long getResultDays()
    {
        return resultDays;
    }

    public void setResultDays(long resultDays)
    {
        this.resultDays = resultDays;
    }

    public String getPrettyResultDays()
    {
        return getPrettyDays(resultDays);
    }

    public boolean equals(Object other)
    {
        if (other instanceof AgeBuildResultCleanupPolicy)
        {
            AgeBuildResultCleanupPolicy p = (AgeBuildResultCleanupPolicy) other;
            return p.workDirDays == workDirDays && p.resultDays == resultDays;
        }

        return false;
    }

    private String getPrettyDays(long days)
    {
        if (days == NEVER_CLEAN)
        {
            return "never";
        }
        else
        {
            return "after " + days + " days";
        }
    }

}
