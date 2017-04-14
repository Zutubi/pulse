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

import com.zutubi.pulse.core.test.api.PulseTestCase;

public class TestResultSummaryTest extends PulseTestCase
{
    public void testToStringAllPassed()
    {
        toStringHelper("5 passed", 0, 0, 0, 0, 5);
    }

    public void testToStringFailures()
    {
        toStringHelper("1 of 5 broken", 0, 1, 0, 0, 5);
    }

    public void testToStringErrors()
    {
        toStringHelper("2 of 5 broken", 0, 0, 2, 0, 5);
    }

    public void testToStringFailuresAndErrors()
    {
        toStringHelper("3 of 5 broken", 0, 1, 2, 0, 5);
    }

    public void testToStringSkipped()
    {
        toStringHelper("3 passed (2 skipped)", 0, 0, 0, 2, 5);
    }

    public void testToStringBrokenAndSkipped()
    {
        toStringHelper("2 of 3 broken (2 skipped)", 0, 1, 1, 2, 5);
    }

    private void toStringHelper(String expected, int expectedFailures, int errors, int failures, int skipped, int total)
    {
        TestResultSummary summary = new TestResultSummary(expectedFailures, errors, failures, skipped, total);
        assertEquals(expected, summary.toString());
    }

    public void testSuccessRateAllPassed()
    {
        successRateHelper("100.00", 0, 0, 0, 0, 5);
    }

    public void testSuccessRateSomeBroken()
    {
        successRateHelper("40.00", 0, 2, 1, 0, 5);
    }

    public void testSuccessRateSomeSkipped()
    {
        successRateHelper("100.00", 0, 0, 0, 2, 5);
    }

    public void testSuccessRateSomeSkippedAndBroken()
    {
        successRateHelper("50.00", 0, 1, 1, 1, 5);
    }
    
    private void successRateHelper(String expected, int expectedFailures, int errors, int failures, int skipped, int total)
    {
        TestResultSummary summary = new TestResultSummary(expectedFailures, errors, failures, skipped, total);
        assertEquals(expected, summary.getSuccessRate());
    }
}
