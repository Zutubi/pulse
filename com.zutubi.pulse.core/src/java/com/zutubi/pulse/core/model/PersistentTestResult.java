package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.postprocessors.api.TestResult;
import com.zutubi.util.time.TimeStamps;

/**
 * Abstract base for test suites and cases.
 */
public abstract class PersistentTestResult
{
    /**
     * Name of the test.
     */
    private String name;
    /**
     * Number of milliseconds it took to execute this test, or
     * UNKNOWN_DURATION if this information is not available.
     */
    private long duration;

    protected PersistentTestResult()
    {
    }

    protected PersistentTestResult(String name)
    {
        this(name, TestResult.DURATION_UNKNOWN);
    }

    protected PersistentTestResult(String name, long duration)
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

    public abstract int getExpectedFailures();

    public abstract int getErrors();

    public abstract int getFailures();

    public abstract int getSkipped();

    public abstract int getTotal();

    public abstract boolean isSuite();

    public boolean hasBrokenTests()
    {
        return getSummary().getBroken() > 0;
    }

    public boolean allTestsSkipped()
    {
        return getSkipped() == getTotal();
    }

    public TestResultSummary getSummary()
    {
        return new TestResultSummary(getExpectedFailures(), getErrors(), getFailures(), getSkipped(), getTotal());
    }

    public void accumulateSummary(TestResultSummary summary)
    {
        summary.addExpectedFailures(getExpectedFailures());
        summary.addErrors(getErrors());
        summary.addFailures(getFailures());
        summary.addSkipped(getSkipped());
        summary.addTotal(getTotal());
    }

    public boolean isEquivalent(PersistentTestResult other)
    {
        if(!getName().equals(other.getName()))
        {
            return false;
        }

        return getDuration() == other.getDuration();
    }
}
