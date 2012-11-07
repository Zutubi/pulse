package com.zutubi.pulse.core.postprocessors.api;

/**
 * Base class for test results with the common parts shared by suites and
 * cases.
 */
public abstract class TestResult
{
    /**
     * Value used when the duration is not known.
     */
    public static final long DURATION_UNKNOWN = -1;
    
    private String name;
    private long duration;

    /**
     * Creates a result with the given name and an unknown duration.
     *
     * @param name name of the suite or case
     */
    public TestResult(String name)
    {
        this(name, DURATION_UNKNOWN);
    }

    /**
     * Creates a result with the given name and duration.
     *
     * @param name     name of the suite or case
     * @param duration time it took to execute the suite or case, in
     *                 milliseconds
     */
    public TestResult(String name, long duration)
    {
        this.name = name;
        this.duration = duration;
    }

    /**
     * @return the name of the suite or case
     */
    public String getName()
    {
        return name;
    }

    /**
     * Updates the name of this result.
     *
     * @param name the new name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the time it took to execute the suite or case, in milliseconds,
     *         may be {@link #DURATION_UNKNOWN}
     */
    public long getDuration()
    {
        return duration;
    }

    /**
     * Updates the duration.
     *
     * @param duration time it took to execute the suite or case, in
     *                 milliseconds
     */
    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        TestResult that = (TestResult) o;

        if (duration != that.duration)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        return result;
    }
}
