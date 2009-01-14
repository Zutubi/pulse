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
     * Number of cases that were skipped.
     */
    private int skipped;
    /**
     * Total number of test cases.
     */
    private int total;

    public TestResultSummary()
    {
        errors = 0;
        failures = 0;
        skipped = 0;
        total = 0;
    }

    public TestResultSummary(int errors, int failures, int skipped, int total)
    {
        this.errors = errors;
        this.failures = failures;
        this.skipped = skipped;
        this.total = total;
    }

    public int getErrors()
    {
        return errors;
    }

    public boolean hasErrors()
    {
        return getErrors() > 0;
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

    public boolean hasFailures()
    {
        return getFailures() > 0;
    }
    
    public void setFailures(int failures)
    {
        this.failures = failures;
    }

    public void addFailures(int failures)
    {
        this.failures += failures;
    }

    public int getSkipped()
    {
        return skipped;
    }

    public boolean hasSkipped()
    {
        return getSkipped() > 0;
    }

    public void setSkipped(int skipped)
    {
        this.skipped = skipped;
    }

    public void addSkipped(int skipped)
    {
        this.skipped += skipped;
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
        return total - getBroken() - skipped;
    }

    public int getBroken()
    {
        return errors + failures;
    }

    public boolean hasBroken()
    {
        return getBroken() > 0;
    }

    public boolean allPassed()
    {
        return getPassed() == getTotal();
    }

    public int hashCode()
    {
        return 1000000 * skipped + errors * 10000 + failures * 100 + total;
    }

    public boolean equals(Object obj)
    {
        if(obj instanceof TestResultSummary)
        {
            TestResultSummary other = (TestResultSummary) obj;
            return other.errors == errors && other.failures == failures && other.skipped == skipped && other.total == total;
        }

        return false;
    }

    public void add(TestResultSummary summary)
    {
        addErrors(summary.errors);
        addFailures(summary.failures);
        addSkipped(summary.skipped);
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
            rate = getPassed() * 100.0 / (total - skipped);
        }
        return String.format("%.2f", rate);
    }


    public String toString()
    {
        String result;
        if (hasTests())
        {
            if (hasBroken())
            {
                result = Integer.toString(getBroken()) + " of " + (total  - skipped) + " broken";
            }
            else
            {
                result = total - skipped + " passed";
            }

            if (hasSkipped())
            {
                result += " (" + skipped + " skipped)";
            }
        }
        else
        {
            result = "none";
        }

        return result;
    }
}
