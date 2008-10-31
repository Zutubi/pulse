package com.zutubi.pulse.core.model;

/**
 * Stores historical information about a test case.
 */
public class TestCaseIndex extends Entity
{
    private long projectId;
    private long stageNameId;
    private String name;
    private int totalCount;
    private int failureCount;
    private int errorCount;
    private long brokenSince = 0;
    private long brokenNumber = 0;

    public TestCaseIndex()
    {
    }

    public TestCaseIndex(long projectId, long stageNameId, String name)
    {
        this.projectId = projectId;
        this.stageNameId = stageNameId;
        this.name = name;
    }

    public void recordExecution(TestCaseResult.Status status, long buildId, long buildNumber)
    {
        totalCount++;
        switch (status)
        {
            case ERROR:
                errorCount++;
                checkBroken(buildId, buildNumber);
                break;
            case FAILURE:
                failureCount++;
                checkBroken(buildId, buildNumber);
                break;
            case PASS:
                brokenSince = 0;
                brokenNumber = 0;
                break;
        }
    }

    private void checkBroken(long buildId, long buildNumber)
    {
        if (isHealthy())
        {
            brokenSince = buildId;
            brokenNumber = buildNumber;
        }
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public long getStageNameId()
    {
        return stageNameId;
    }

    public void setStageNameId(long stageNameId)
    {
        this.stageNameId = stageNameId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(int totalCount)
    {
        this.totalCount = totalCount;
    }

    public int getFailureCount()
    {
        return failureCount;
    }

    public void setFailureCount(int failureCount)
    {
        this.failureCount = failureCount;
    }

    public int getErrorCount()
    {
        return errorCount;
    }

    public void setErrorCount(int errorCount)
    {
        this.errorCount = errorCount;
    }

    public boolean isHealthy()
    {
        return brokenSince == 0;
    }

    public long getBrokenSince()
    {
        return brokenSince;
    }

    public void setBrokenSince(long brokenSince)
    {
        this.brokenSince = brokenSince;
    }

    public long getBrokenNumber()
    {
        return brokenNumber;
    }

    public void setBrokenNumber(long brokenNumber)
    {
        this.brokenNumber = brokenNumber;
    }
}
