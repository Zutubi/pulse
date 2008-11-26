package com.zutubi.pulse.core.postprocessors.api;

/**
 */
public class TestCaseResult extends TestResult
{
    private TestStatus status;
    private String message;

    public TestCaseResult(String name)
    {
        this(name, TestStatus.PASS);
    }

    public TestCaseResult(String name, TestStatus status)
    {
        this(name, DURATION_UNKNOWN, status);
    }

    public TestCaseResult(String name, long duration, TestStatus status)
    {
        this(name, duration, status, null);
    }

    public TestCaseResult(String name, long duration, TestStatus status, String message)
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

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
