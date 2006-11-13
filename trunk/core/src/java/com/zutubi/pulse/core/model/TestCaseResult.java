package com.zutubi.pulse.core.model;

/**
 * Represents the result of a single test case.
 */
public class TestCaseResult extends TestResult
{
    public enum Status
    {
        FAILURE,
        ERROR,
        PASS
    }

    /**
     * The status of the case (i.e. the actual result).
     */
    private Status status;
    /**
     * Error/failure message when things have gone pear-shaped.
     */
    private String message;

    public TestCaseResult()
    {
    }

    public TestCaseResult(String name)
    {
        this(name, UNKNOWN_DURATION);
    }

    public TestCaseResult(String name, long duration)
    {
        this(name, duration, Status.PASS, null);
    }

    public TestCaseResult(String name, long duration, Status status, String message)
    {
        super(name, duration);
        this.status = status;
        this.message = message;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public String getStatusName()
    {
        return status.toString();
    }

    public void setStatusName(String statusName)
    {
        status = Status.valueOf(statusName);
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public int getErrors()
    {
        return status == Status.ERROR ? 1 : 0;
    }

    public int getFailures()
    {
        return status == Status.FAILURE ? 1 : 0;
    }

    public int getTotal()
    {
        return 1;
    }

    public boolean isSuite()
    {
        return false;
    }

    public boolean isEquivalent(TestResult otherResult)
    {
        if(!(otherResult instanceof TestCaseResult))
        {
            return false;
        }

        if(!super.isEquivalent(otherResult))
        {
            return false;
        }

        TestCaseResult other = (TestCaseResult) otherResult;
        if(message == null)
        {
            if(other.message != null)
            {
                return false;
            }
        }
        else
        {
            if(!message.equals(other.message))
            {
                return false;
            }
        }

        return status == other.status;
    }
}
