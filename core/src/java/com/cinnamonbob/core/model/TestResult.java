package com.cinnamonbob.core.model;

/**
 * Abstract base for test suites and cases.
 */
public abstract class TestResult extends Entity
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

    public abstract int getErrors();
    public abstract int getFailures();
    public abstract int getTotal();

}
