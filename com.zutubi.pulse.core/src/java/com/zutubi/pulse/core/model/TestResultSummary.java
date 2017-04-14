/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.model;

import com.zutubi.i18n.Messages;

/**
 * Provides a small amoutn of summary information for a group of tests
 * results.
 */
public class TestResultSummary extends Entity
{
    private static final Messages I18N = Messages.getInstance(TestResultSummary.class);

    private static final String KEY_ALL_PASSED = "tests.all.passed";
    private static final String KEY_SOME_BROKEN = "tests.some.broken";
    private static final String KEY_SOME_SKIPPED = "tests.some.skipped";
    private static final String KEY_NONE= "tests.none";

    /**
     * Number of cases with expected failure status.
     */
    private int expectedFailures;
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
        expectedFailures = 0;
        errors = 0;
        failures = 0;
        skipped = 0;
        total = 0;
    }

    public TestResultSummary(int expectedFailures, int errors, int failures, int skipped, int total)
    {
        this.expectedFailures = expectedFailures;
        this.errors = errors;
        this.failures = failures;
        this.skipped = skipped;
        this.total = total;
    }

    public int getExpectedFailures()
    {
        return expectedFailures;
    }

    public boolean hasExpectedFailures()
    {
        return getExpectedFailures() > 0;
    }

    public void setExpectedFailures(int expectedFailures)
    {
        this.expectedFailures = expectedFailures;
    }

    public void addExpectedFailures(int expectedFailures)
    {
        this.expectedFailures += expectedFailures;
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
        return expectedFailures + errors + failures;
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
        return 100000000 * expectedFailures + 1000000 * skipped + errors * 10000 + failures * 100 + total;
    }

    public boolean equals(Object obj)
    {
        if(obj instanceof TestResultSummary)
        {
            TestResultSummary other = (TestResultSummary) obj;
            return other.expectedFailures == expectedFailures && other.errors == errors && other.failures == failures && other.skipped == skipped && other.total == total;
        }

        return false;
    }

    public void add(TestResultSummary summary)
    {
        addExpectedFailures(summary.expectedFailures);
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

    public double getSuccessPercent()
    {
        double rate = 100D;
        if (hasTests())
        {
            rate = getPassed() * 100.0 / (total - skipped);
        }
        return rate;
    }

    public String getSuccessRate()
    {
        // default the success rate to 100. If we have some tests (usually the case but not always),
        // then determine the percentage.
        return String.format("%.2f", getSuccessPercent());
    }

    public String format(Messages messages)
    {
        String result;
        if (hasTests())
        {
            if (hasBroken())
            {
                result = messages.format(KEY_SOME_BROKEN, getBroken() , getTotal() - getSkipped());
            }
            else
            {
                result = messages.format(KEY_ALL_PASSED, getTotal() - getSkipped());
            }

            if (hasSkipped())
            {
                result += " " + messages.format(KEY_SOME_SKIPPED, getSkipped());
            }
        }
        else
        {
            result = messages.format(KEY_NONE);
        }

        return result;
    }

    public String toString()
    {
        return format(I18N);
    }
}
