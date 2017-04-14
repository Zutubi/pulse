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

package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.TestCaseResult;
import com.zutubi.pulse.core.postprocessors.api.TestResult;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.*;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;
import com.zutubi.pulse.core.postprocessors.api.XMLTestPostProcessorTestCase;

import java.io.IOException;

public class JUnitReportPostProcessorTest extends XMLTestPostProcessorTestCase
{
    private JUnitReportPostProcessor pp = new JUnitReportPostProcessor(new JUnitReportPostProcessorConfiguration());

    public void testSimple() throws Exception
    {
        TestSuiteResult tests = runProcessor("simple");

        assertEquals(2, tests.getSuites().size());
        checkWarning(tests.getSuites().get(0), "com.zutubi.pulse.junit.EmptyTest", 91, "No tests found");

        TestSuiteResult suite = tests.getSuites().get(1);
        assertEquals("com.zutubi.pulse.junit.SimpleTest", suite.getName());
        assertEquals(90, suite.getDuration());

        TestCaseResult[] children = suite.getCases().toArray(new TestCaseResult[suite.getCases().size()]);
        assertEquals(3, children.length);
        assertEquals(new TestCaseResult("testSimple", 0, PASS), children[0]);
        assertEquals(new TestCaseResult("testAssertionFailure", 10, FAILURE,
                "junit.framework.AssertionFailedError: expected:<1> but was:<2>\n" +
                        "\tat com.zutubi.pulse.junit.SimpleTest.testAssertionFailure(Unknown Source)"),
                children[1]);
        assertEquals(new TestCaseResult("testThrowException", 10, ERROR,
                "java.lang.RuntimeException: random message\n" +
                        "\tat com.zutubi.pulse.junit.SimpleTest.testThrowException(Unknown Source)"),
                children[2]);
    }

    public void testRandomJunkIgnored() throws Exception
    {
        TestSuiteResult tests = runProcessor("testRandomJunkIgnored");

        assertEquals(2, tests.getSuites().size());
        checkWarning(tests.getSuites().get(0), "com.zutubi.pulse.junit.EmptyTest", 91, "No tests found");

        TestSuiteResult suite = tests.getSuites().get(1);
        assertEquals("com.zutubi.pulse.junit.SimpleTest", suite.getName());
        assertEquals(90, suite.getDuration());

        TestCaseResult[] children = suite.getCases().toArray(new TestCaseResult[suite.getCases().size()]);
        assertEquals(3, children.length);
        assertEquals(new TestCaseResult("testSimple", (long) 0, PASS, null), children[0]);
        assertEquals(new TestCaseResult("testAssertionFailure", (long) 10, FAILURE, "junit.framework.AssertionFailedError: expected:<1> but was:<2>\n" +
                "\tat com.zutubi.pulse.junit.SimpleTest.testAssertionFailure(Unknown Source)"), children[1]);
        assertEquals(new TestCaseResult("testThrowException", (long) 10, ERROR, "java.lang.RuntimeException: random message\n" +
                "\tat com.zutubi.pulse.junit.SimpleTest.testThrowException(Unknown Source)"), children[2]);
    }

    public void testSingle() throws Exception
    {
        TestSuiteResult tests = runProcessor("single");
        assertSingleSuite(tests);
    }

    public void testSuite() throws Exception
    {
        TestSuiteResult tests = runProcessor("suite");
        assertEquals(274, tests.getTotal());
        assertNotNull(tests.findSuite("com.zutubi.pulse.acceptance.AcceptanceTestSuite").findCase("com.zutubi.pulse.acceptance.LicenseAuthorisationAcceptanceTest.testAddProjectLinkOnlyAvailableWhenLicensed"));
    }

    public void testCustom() throws Exception
    {
        JUnitReportPostProcessorConfiguration ppConfig = pp.getConfig();
        ppConfig.setSuiteElement("customtestsuite");
        ppConfig.setCaseElement("customtestcase");
        ppConfig.setFailureElement("customfailure");
        ppConfig.setErrorElement("customerror");
        ppConfig.setSkippedElement("customskipped");
        ppConfig.setNameAttribute("customname");
        ppConfig.setPackageAttribute("custompackage");
        ppConfig.setTimeAttribute("customtime");
        TestSuiteResult tests = runProcessor("custom");
        assertSingleSuite(tests);
    }

    private void assertSingleSuite(TestSuiteResult tests)
    {
        assertEquals(1, tests.getSuites().size());

        TestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorTest", suite.getName());
        assertEquals(391, suite.getDuration());

        TestCaseResult[] children = suite.getCases().toArray(new TestCaseResult[suite.getCases().size()]);
        assertEquals(4, children.length);
        assertEquals(new TestCaseResult("testSimple", (long) 291, PASS, null), children[0]);
        assertEquals(new TestCaseResult("testSkipped", (long) 0, SKIPPED, null), children[1]);
        assertEquals(new TestCaseResult("testFailure", (long) 10, FAILURE, "junit.framework.AssertionFailedError\n" +
                        "\tat\n" +
                        "        com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorTest.testFailure(JUnitReportPostProcessorTest.java:63)"), children[2]);
        assertEquals(new TestCaseResult("testError", (long) 0, ERROR, "java.lang.RuntimeException: whoops!\n" +
                        "\tat\n" +
                        "        com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorTest.testError(JUnitReportPostProcessorTest.java:68)"), children[3]);
    }

    public void testNoMessage() throws Exception
    {
        TestSuiteResult tests = runProcessor("nomessage");
        TestSuiteResult suite = tests.findSuite("com.zutubi.pulse.junit.NoMessages");
        checkStatusCounts(suite, "com.zutubi.pulse.junit.NoMessages", 2, 2, 0, 0, 0);
        long duration1 = -1;
        assertEquals(new TestCaseResult("testFailureNoMessageAtAll", duration1, FAILURE, null), suite.findCase("testFailureNoMessageAtAll"));
        long duration = -1;
        assertEquals(new TestCaseResult("testFailureMessageInAttribute", duration, FAILURE, "this message only"), suite.findCase("testFailureMessageInAttribute"));
    }

    public void testNested() throws Exception
    {
        TestSuiteResult tests = runProcessor("nested");
        TestSuiteResult suite = tests.findSuite("Outer");
        assertNotNull(suite);
        checkStatusCounts(suite, "Outer", 2, 0, 0, 0, 0);
        TestSuiteResult nested = suite.findSuite("Nested");
        checkStatusCounts(nested, "Nested", 2, 0, 0, 0, 0);
        long duration1 = -1;
        assertEquals(new TestCaseResult("test1", duration1, PASS, null), nested.findCase("test1"));
        long duration = -1;
        assertEquals(new TestCaseResult("test2", duration, PASS, null), nested.findCase("test2"));
    }

    public void testNoSuiteName() throws Exception
    {
        TestSuiteResult tests = runProcessor("nonamesuite");
        assertEquals(0, tests.getSuites().size());
    }

    private TestSuiteResult runProcessor(String name) throws IOException
    {
        return runProcessorAndGetTests(pp, name, EXTENSION_XML);
    }

    private void checkWarning(TestResult testResult, String name, long duration, String contents)
    {
        assertTrue(testResult instanceof TestSuiteResult);
        TestSuiteResult suite = (TestSuiteResult) testResult;
        assertEquals(name, suite.getName());
        assertEquals(duration, suite.getDuration());

        TestCaseResult[] children = suite.getCases().toArray(new TestCaseResult[suite.getCases().size()]);
        assertEquals(1, children.length);
        TestCaseResult caseResult = children[0];
        assertEquals("warning", caseResult.getName());
        assertEquals(10, caseResult.getDuration());
        assertTrue(caseResult.getMessage().contains(contents));
    }
}
