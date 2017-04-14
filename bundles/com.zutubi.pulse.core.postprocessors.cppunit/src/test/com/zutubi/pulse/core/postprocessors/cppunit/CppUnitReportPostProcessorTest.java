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

package com.zutubi.pulse.core.postprocessors.cppunit;

import com.zutubi.pulse.core.postprocessors.api.*;

import java.io.IOException;
import java.util.List;

public class CppUnitReportPostProcessorTest extends XMLTestPostProcessorTestCase
{
    private CppUnitReportPostProcessor pp = new CppUnitReportPostProcessor(new CppUnitReportPostProcessorConfiguration());

    public void testBasic() throws Exception
    {
        TestSuiteResult tests = runProcessor("basic");

        assertEquals(2, tests.getSuites().size());

        TestSuiteResult suite = tests.getSuites().get(0);
        assertAnotherTest(suite, "AnotherTest");

        suite = tests.getSuites().get(1);
        assertTest(suite, "Test");
    }

    public void testTwoReports() throws Exception
    {
        TestSuiteResult tests = runProcessor("basic", "second");

        assertEquals(3, tests.getSuites().size());

        TestSuiteResult suite = tests.getSuites().get(0);
        assertAnotherTest(suite, "AnotherTest");

        suite = tests.getSuites().get(1);
        assertTest(suite, "Test");

        suite = tests.getSuites().get(2);
        assertHelloWorld(suite, "Second");
    }

    public void testEmptyTags() throws Exception
    {
        TestSuiteResult tests = runProcessor("emptytags");

        assertEquals(1, tests.getSuites().size());

        TestSuiteResult suite = tests.getSuites().get(0);
        assertEquals(1, suite.getCases().size());

        TestCaseResult[] children = suite.getCases().toArray(new TestCaseResult[suite.getCases().size()]);
        TestCaseResult caseResult = children[0];
        assertEquals("all", caseResult.getName());
        assertEquals(TestStatus.ERROR, caseResult.getStatus());
    }

    public void testParentSuite() throws Exception
    {
        pp.getConfig().setSuite("parent");
        TestSuiteResult tests = runProcessor("basic");

        List<TestSuiteResult> topLevelSuites = tests.getSuites();
        assertEquals(1, topLevelSuites.size());
        assertEquals("parent", topLevelSuites.get(0).getName());
        
        tests = topLevelSuites.get(0);
        assertEquals(2, tests.getSuites().size());
        TestSuiteResult suite = tests.getSuites().get(0);
        assertAnotherTest(suite, "AnotherTest");

        suite = tests.getSuites().get(1);
        assertTest(suite, "Test");
    }

    public void testRandomJunkIgnored() throws Exception
    {
        TestSuiteResult tests = runProcessor("testRandomJunkIgnored");

        assertEquals(2, tests.getSuites().size());

        TestSuiteResult suite = tests.getSuites().get(0);
        assertAnotherTest(suite, "AnotherTest");

        suite = tests.getSuites().get(1);
        assertTest(suite, "Test");
    }
    
    private void assertHelloWorld(TestSuiteResult suite, String name)
    {
        checkStatusCounts(suite, name, 1, 0, 0, 0, 0);

        TestCaseResult[] children = suite.getCases().toArray(new TestCaseResult[suite.getCases().size()]);
        assertEquals(new TestCaseResult("testHelloWorld", TestResult.DURATION_UNKNOWN, TestStatus.PASS, null), children[0]);
    }

    private void assertTest(TestSuiteResult suite, String name)
    {
        checkStatusCounts(suite, name, 6, 2, 1, 0, 0);

        TestCaseResult[] children = suite.getCases().toArray(new TestCaseResult[suite.getCases().size()]);
        assertEquals(new TestCaseResult("testFailure", TestResult.DURATION_UNKNOWN, TestStatus.FAILURE, "At file cppunit.cpp line 34\n" +
                                                         "assertion failed\n" +
                                                         "- Expression: 1 == 2"), children[0]);
        assertEquals(new TestCaseResult("testThrow", TestResult.DURATION_UNKNOWN, TestStatus.ERROR, "uncaught exception of type std::exception\n" +
                                                     "- St9exception"), children[1]);
        assertEquals(new TestCaseResult("testDidntThrow", TestResult.DURATION_UNKNOWN, TestStatus.FAILURE, "expected exception not thrown\n" +
                                                            "- Expected exception type: std::exception"), children[2]);
        assertEquals(new TestCaseResult("testHelloWorld", TestResult.DURATION_UNKNOWN, TestStatus.PASS, null), children[3]);
        assertEquals(new TestCaseResult("testExpectedThrow", TestResult.DURATION_UNKNOWN, TestStatus.PASS, null), children[4]);
        assertEquals(new TestCaseResult("testExpectedFailure", TestResult.DURATION_UNKNOWN, TestStatus.PASS, null), children[5]);
    }

    private void assertAnotherTest(TestSuiteResult suite, String name)
    {
        checkStatusCounts(suite, name, 1, 0, 0, 0, 0);

        TestCaseResult[] children = suite.getCases().toArray(new TestCaseResult[suite.getCases().size()]);
        assertEquals(new TestCaseResult("testIt", TestResult.DURATION_UNKNOWN, TestStatus.PASS, null), children[0]);
    }

    private TestSuiteResult runProcessor(String... names) throws IOException
    {
        TestSuiteResult result = new TestSuiteResult(null);
        for (String name: names)
        {
            TestSuiteResult suite = runProcessorAndGetTests(pp, name, EXTENSION_XML);
            result.addAllSuites(suite.getSuites());
            result.addAllCases(suite.getCases());
        }
        
        return result;
    }
}
