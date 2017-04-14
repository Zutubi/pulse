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

package com.zutubi.pulse.core.postprocessors.boostregression;

import com.zutubi.pulse.core.postprocessors.api.*;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.FAILURE;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.PASS;

public class BoostRegressionPostProcessorTest extends XMLTestPostProcessorTestCase
{
    private BoostRegressionPostProcessor pp = new BoostRegressionPostProcessor(new BoostRegressionPostProcessorConfiguration());

    public void testBasic() throws Exception
    {
        singleLogHelper("compilefail", "iterator", "interoperable_fail", PASS, null);
    }

    public void testRun() throws Exception
    {
        singleLogHelper("run", "statechart", "InvalidResultCopyTestRelaxed", PASS, null);
    }

    public void testRandomJunkIgnored() throws Exception
    {
        singleLogHelper("testRandomJunkIgnored", "statechart", "InvalidResultCopyTestRelaxed", PASS, null);
    }

    public void testBroken() throws Exception
    {
        singleLogHelper("broken", "statechart", "InvalidResultCopyTestRelaxed", FAILURE, "============================[ compile output below ]============================\n" +
                "    compiler error here\n" +
                "============================[ compile output above ]============================\n");
    }

    public void testNested() throws Exception
    {
        TestSuiteResult tests = runProcessorAndGetTests(pp, "nested", EXTENSION_XML);
        assertEquals(1, tests.getSuites().size());
        TestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("algorithm", suite.getName());
        assertEquals(1, suite.getTotal());
        suite = suite.findSuite("minmax");
        assertNotNull(suite);
        assertEquals(new TestCaseResult("minmax", TestResult.DURATION_UNKNOWN, PASS, null), suite.findCase("minmax"));
    }

    private void singleLogHelper(String testName, String suiteName, String caseName, TestStatus status, String message) throws Exception
    {
        TestSuiteResult tests = runProcessorAndGetTests(pp, testName, EXTENSION_XML);
        assertEquals(1, tests.getSuites().size());
        TestSuiteResult suite = tests.getSuites().get(0);
        assertEquals(suiteName, suite.getName());
        assertEquals(1, suite.getTotal());
        assertEquals(new TestCaseResult(caseName, TestResult.DURATION_UNKNOWN, status, message), suite.findCase(caseName));
    }

}
