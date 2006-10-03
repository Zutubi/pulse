package com.zutubi.pulse.core.model;

import com.zutubi.pulse.util.TimeStamps;

/**
 * Abstract base for test suites and cases.
 */
public abstract class TestResult
{
    public static final long UNKNOWN_DURATION = -1;

    /**
     * Name of the test.
     */
    private String name;
    /**
     * Number of milliseconds it took to execute this test, or
     * UNKNOWN_DURATION if this information is not available.
     */
    private long duration;

    protected TestResult()
    {
    }

    protected TestResult(String name)
    {
        this(name, UNKNOWN_DURATION);
    }

    protected TestResult(String name, long duration)
    {
        this.name = name;
        this.duration = duration;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public long getDuration()
    {
        return duration;
    }

    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    public String getPrettyDuration()
    {
        if(duration >= 0)
        {
            return TimeStamps.getPrettyElapsed(duration);
        }
        else
        {
            return "";
        }
    }

    public abstract int getErrors();

    public abstract int getFailures();

    public abstract int getTotal();

    public abstract boolean isSuite();

    public boolean hasBrokenTests()
    {
        return getSummary().getBroken() > 0;
    }

    public TestResultSummary getSummary()
    {
        return new TestResultSummary(getErrors(), getFailures(), getTotal());
    }

    public void accumulateSummary(TestResultSummary summary)
    {
        summary.addErrors(getErrors());
        summary.addFailures(getFailures());
        summary.addTotal(getTotal());
    }

    public boolean isEquivalent(TestResult other)
    {
        if(!getName().equals(other.getName()))
        {
            return false;
        }

        return getDuration() == other.getDuration();
    }
}
