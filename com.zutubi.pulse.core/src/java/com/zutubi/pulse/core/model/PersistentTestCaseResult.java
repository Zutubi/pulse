package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.postprocessors.api.TestResult;
import com.zutubi.pulse.core.postprocessors.api.TestStatus;

/**
 * Represents the result of a single test case.
 */
public class PersistentTestCaseResult extends PersistentTestResult
{
    /**
     * The status of the case (i.e. the actual result).
     */
    private TestStatus status;
    /**
     * Error/failure message when things have gone pear-shaped.
     */
    private String message;
    /**
     * If broken in an earlier build, id of that build.  Only available after
     * indexing.
     */
    private long brokenSince = 0;
    /**
     * If broken in an earlier build, number of that build.  Only available
     * after indexing.
     */
    private long brokenNumber = 0;
    /**
     * True if fixed in this build.  Only available after indexing.
     */
    private boolean fixed = false;

    
    public PersistentTestCaseResult()
    {
    }

    public PersistentTestCaseResult(String name)
    {
        this(name, TestResult.DURATION_UNKNOWN);
    }

    public PersistentTestCaseResult(String name, long duration)
    {
        this(name, duration, TestStatus.PASS, null);
    }

    public PersistentTestCaseResult(String name, long duration, TestStatus status, String message)
    {
        super(name, duration);
        this.status = status;
        this.message = message;
    }
    
    public TestStatus getStatus()
    {
        return status;
    }

    public void setStatus(TestStatus status)
    {
        this.status = status;
    }

    public String getStatusName()
    {
        return status.toString();
    }

    public void setStatusName(String statusName)
    {
        status = TestStatus.valueOf(statusName);
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    @Override
    public int getExpectedFailures()
    {
        return status == TestStatus.EXPECTED_FAILURE ? 1 : 0;
    }

    public int getErrors()
    {
        return status == TestStatus.ERROR ? 1 : 0;
    }

    public int getFailures()
    {
        return status == TestStatus.FAILURE ? 1 : 0;
    }

    public int getSkipped()
    {
        return status == TestStatus.SKIPPED ? 1 : 0;
    }

    public int getTotal()
    {
        return 1;
    }

    public boolean isSuite()
    {
        return false;
    }

    public boolean wasBrokenPreviously()
    {
        return brokenSince != 0;
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

    public boolean isFixed()
    {
        return fixed;
    }

    public void setFixed(boolean fixed)
    {
        this.fixed = fixed;
    }

    public boolean isEquivalent(PersistentTestResult otherResult)
    {
        if(!(otherResult instanceof PersistentTestCaseResult))
        {
            return false;
        }

        if(!super.isEquivalent(otherResult))
        {
            return false;
        }

        PersistentTestCaseResult other = (PersistentTestCaseResult) otherResult;
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
