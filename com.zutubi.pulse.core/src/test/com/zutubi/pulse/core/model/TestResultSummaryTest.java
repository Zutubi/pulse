package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.test.api.PulseTestCase;

public class TestResultSummaryTest extends PulseTestCase
{
    public void testToStringAllPassed()
    {
        toStringHelper("5 passed", 0, 0, 0, 5);
    }

    public void testToStringFailures()
    {
        toStringHelper("1 of 5 broken", 1, 0, 0, 5);
    }

    public void testToStringErrors()
    {
        toStringHelper("2 of 5 broken", 0, 2, 0, 5);
    }

    public void testToStringFailuresAndErrors()
    {
        toStringHelper("3 of 5 broken", 1, 2, 0, 5);
    }

    public void testToStringSkipped()
    {
        toStringHelper("3 passed (2 skipped)", 0, 0, 2, 5);
    }

    public void testToStringBrokenAndSkipped()
    {
        toStringHelper("2 of 3 broken (2 skipped)", 1, 1, 2, 5);
    }

    private void toStringHelper(String expected, int errors, int failures, int skipped, int total)
    {
        TestResultSummary summary = new TestResultSummary(errors, failures, skipped, total);
        assertEquals(expected, summary.toString());
    }

    public void testSuccessRateAllPassed()
    {
        successRateHelper("100.00", 0, 0, 0, 5);
    }

    public void testSuccessRateSomeBroken()
    {
        successRateHelper("40.00", 2, 1, 0, 5);
    }

    public void testSuccessRateSomeSkipped()
    {
        successRateHelper("100.00", 0, 0, 2, 5);
    }

    public void testSuccessRateSomeSkippedAndBroken()
    {
        successRateHelper("50.00", 1, 1, 1, 5);
    }
    
    private void successRateHelper(String expected, int errors, int failures, int skipped, int total)
    {
        TestResultSummary summary = new TestResultSummary(errors, failures, skipped, total);
        assertEquals(expected, summary.getSuccessRate());
    }
}
