package com.zutubi.pulse.core.model;

/**
 * Provides a small amoutn of summary information for a group of tests
 * results.
 */
public class TestResultSummary extends Entity
{
    /**
     * Number of cases with failure status.
     */
    private int errors;
    /**
     * Number of cases with error status.
     */
    private int failures;
    /**
     * Total number of test cases.
     */
    private int total;

    public TestResultSummary()
    {
        errors = 0;
        failures = 0;
        total = 0;
    }

    public TestResultSummary(int errors, int failures, int total)
    {
        this.errors = errors;
        this.failures = failures;
        this.total = total;
    }

    public int getErrors()
    {
        return errors;
    }

    public void setErrors(int errors)
    {
        this.errors = errors;
    }

    public void addErrors(int errors)
    {
        this.errors += errors;
    }

    public int getFailures()
    {
        return failures;
    }

    public void setFailures(int failures)
    {
        this.failures = failures;
    }

    public void addFailures(int failures)
    {
        this.failures += failures;
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public void addTotal(int total)
    {
        this.total += total;
    }

    public int getPassed()
    {
        return total - getBroken();
    }

    public int getBroken()
    {
        return errors + failures;
    }

    public boolean allPassed()
    {
        return errors == 0 && failures == 0;
    }

    public int hashCode()
    {
        return errors * 10000 + failures * 100 + total;
    }

    public boolean equals(Object obj)
    {
        if(obj instanceof TestResultSummary)
        {
            TestResultSummary other = (TestResultSummary) obj;
            return other.errors == errors && other.failures == failures && other.total == total;
        }

        return false;
    }

    public void add(TestResultSummary summary)
    {
        addErrors(summary.errors);
        addFailures(summary.failures);
        addTotal(summary.total);
    }

    /**
     * This method indicates whether or not this test result summary contains any test results.
     *
     * @return true if the number of tests in this summary is greater than zero, false otherwise.
     */
    public boolean hasTests()
    {
        return total > 0;
    }

    public String getSuccessRate()
    {
        // default the success rate to zero. If we have some tests (usually the case but not always),
        // then determine the percentage. 
        double rate = 0D;
        if (hasTests())
        {
            rate = getPassed() * 100.0 / total;
        }
        return String.format("%.2f", rate);
    }
}
