package com.zutubi.pulse.core.postprocessors.api;

/**
 */
public class TestResult
{
    public static final long DURATION_UNKNOWN = -1;
    
    private String name;
    private long duration;

    public TestResult(String name)
    {
        this(name, DURATION_UNKNOWN);
    }

    public TestResult(String name, long duration)
    {
        this.name = name;
        this.duration = duration;
    }

    public String getName()
    {
        return name;
    }

    public long getDuration()
    {
        return duration;
    }

    public void setDuration(long duration)
    {
        this.duration = duration;
    }
}
