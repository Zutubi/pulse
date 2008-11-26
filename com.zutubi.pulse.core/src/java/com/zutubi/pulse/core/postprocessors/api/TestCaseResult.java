package com.zutubi.pulse.core.postprocessors.api;

/**
 * Represents the result of executing a single test case.  Cases are usually
 * combined into larger {@link com.zutubi.pulse.core.postprocessors.api.TestSuiteResult}s.
 */
public class TestCaseResult extends TestResult
{
    private TestStatus status;
    private String message;

    /**
     * Creates a passing test case with the given name but no known duration.
     *
     * @param name the name of the test case
     */
    public TestCaseResult(String name)
    {
        this(name, TestStatus.PASS);
    }

    /**
     * Creates a test case with the given name and status, but no known
     * duration.
     *
     * @param name   the name of the test case
     * @param status status that the test completed with
     */
    public TestCaseResult(String name, TestStatus status)
    {
        this(name, DURATION_UNKNOWN, status);
    }

    /**
     * Creates a test case with the given name, duration and status.
     *
     * @param name     the name of the test case
     * @param duration time, in milliseconds, that the case took to execute
     * @param status   status that the test completed with
     */
    public TestCaseResult(String name, long duration, TestStatus status)
    {
        this(name, duration, status, null);
    }

    /**
     * Creates a test case with the given name, duration, status and detail
     * message.  The message is used to indicate the reason for failed cases.
     *
     * @param name     the name of the test case
     * @param duration time, in milliseconds, that the case took to execute
     * @param status   status that the test completed with
     * @param message  message indicating what caused the test to fail
     */
    public TestCaseResult(String name, long duration, TestStatus status, String message)
    {
        super(name, duration);
        this.status = status;
        this.message = message;
    }

    /**
     * @return the status that the test completed with
     */
    public TestStatus getStatus()
    {
        return status;
    }

    /**
     * Updates the status.
     *
     * @param status status that the test completed with
     */
    public void setStatus(TestStatus status)
    {
        this.status = status;
    }

    /**
     * @return a message indicating what caused the test to fail
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Updates the message.
     *
     * @param message message indicating what caused the test to fail
     */
    public void setMessage(String message)
    {
        this.message = message;
    }
}
