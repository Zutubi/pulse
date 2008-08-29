package com.zutubi.pulse.core.model;

import com.zutubi.pulse.test.PulseTestCase;

import java.util.List;

/**
 */
public class TestSuiteResultTest extends PulseTestCase
{
    private TestSuiteResult suite;


    protected void setUp() throws Exception
    {
        suite = new TestSuiteResult("test", 10);
    }

    protected void tearDown() throws Exception
    {
        suite = null;
    }

    public void testAddCase()
    {
        TestCaseResult childCase = createPassCase("acase");
        suite.add(childCase, TestSuiteResult.Resolution.OFF);
        assertEquals(1, suite.getTotal());
        assertTrue(suite.getCases().iterator().next() == childCase);
    }

    public void testAddSuite()
    {
        TestSuiteResult childSuite = new TestSuiteResult("child suite");
        suite.add(childSuite, TestSuiteResult.Resolution.OFF);
        List<TestSuiteResult> children = suite.getSuites();
        assertEquals(1, children.size());
        assertTrue(children.get(0) == childSuite);
    }

    public void testAddCaseAlreadyExistsResolutionOff()
    {
        TestCaseResult[] cases = addCaseHelper(TestSuiteResult.Resolution.OFF, createPassCase("acase"), createPassCase("acase"));
        assertEquals(1, cases.length);
        assertCases(cases, createPassCase("acase"));
    }

    public void testAddCaseAlreadyExistsResolutionAppend()
    {
        TestCaseResult[] cases = addCaseHelper(TestSuiteResult.Resolution.APPEND, createPassCase("acase"), createPassCase("acase"));
        assertCases(cases, createPassCase("acase"), createPassCase("acase2"));
    }

    public void testAddCaseAlreadyExistsResolutionPrepend()
    {
        TestCaseResult[] cases = addCaseHelper(TestSuiteResult.Resolution.PREPEND, createPassCase("acase"), createPassCase("acase"));
        assertCases(cases, createPassCase("acase"), createPassCase("2acase"));
    }

    private TestCaseResult[] addCaseHelper(TestSuiteResult.Resolution resolution, TestCaseResult... cases)
    {
        for (TestCaseResult caseResult: cases)
        {
            suite.add(caseResult, resolution);
        }

        return suite.getCases().toArray(new TestCaseResult[suite.getCases().size()]);
    }

    public void testAddCaseLessSevereAlreadyExistsResolutionOff()
    {
        TestCaseResult[] children = addCaseHelper(TestSuiteResult.Resolution.OFF, createPassCase("acase"), createFailureCase("acase"));
        assertCases(children, createFailureCase("acase"));
        assertFalse(children[0].isEquivalent(createPassCase("acase")));
    }

    public void testAddCaseLessSevereAlreadyExistsResolution()
    {
        TestCaseResult[] children = addCaseHelper(TestSuiteResult.Resolution.APPEND, createPassCase("acase"), createFailureCase("acase"));
        assertCases(children, createPassCase("acase"), createFailureCase("acase2"));
    }

    public void testAddCaseMoreSevereAlreadyExistsResolutionOff()
    {
        TestCaseResult[] children = addCaseHelper(TestSuiteResult.Resolution.OFF, createFailureCase("acase"), createPassCase("acase"));
        assertCases(children, createFailureCase("acase"));
        assertFalse(children[0].isEquivalent(createPassCase("acase")));
    }

    public void testAddCaseMoreSevereAlreadyExistsResolutionAppend()
    {
        TestCaseResult[] children = addCaseHelper(TestSuiteResult.Resolution.APPEND, createFailureCase("acase"), createPassCase("acase"));
        assertCases(children, createFailureCase("acase"), createPassCase("acase2"));
    }

    public void testAddSuiteAlreadyExists()
    {
        TestSuiteResult childSuite = new TestSuiteResult("child suite");
        TestSuiteResult childSuite2 = new TestSuiteResult("child suite");
        TestCaseResult childCase = createPassCase("acase");
        TestCaseResult childCase2 = createFailureCase("anothercase");
        childSuite.add(childCase, TestSuiteResult.Resolution.OFF);
        childSuite.add(childCase2, TestSuiteResult.Resolution.OFF);
        suite.add(childSuite, TestSuiteResult.Resolution.OFF);
        suite.add(childSuite2, TestSuiteResult.Resolution.OFF);

        List<TestSuiteResult> children = suite.getSuites();
        assertEquals(1, children.size());
        TestSuiteResult suiteResult = children.get(0);
        assertEquals(2, suiteResult.getCases().size());
        assertTrue(suiteResult.getCase(childCase.getName()).isEquivalent(childCase));
        assertTrue(suiteResult.getCase(childCase2.getName()).isEquivalent(childCase2));
    }

    public void testAddSuiteAlreadyExistsOverlappingCaseResolutionOff()
    {
        addSuiteAlreadyExistsOverlappingHelper(TestSuiteResult.Resolution.OFF);

        List<TestSuiteResult> children = suite.getSuites();
        assertEquals(1, children.size());
        TestSuiteResult suiteResult = children.get(0);
        TestCaseResult[] cases = suiteResult.getCases().toArray(new TestCaseResult[suiteResult.getCases().size()]);
        assertCases(cases, createFailureCase("acase"));
    }

    public void testAddSuiteAlreadyExistsOverlappingCaseResolutionAppend()
    {
        addSuiteAlreadyExistsOverlappingHelper(TestSuiteResult.Resolution.APPEND);

        List<TestSuiteResult> children = suite.getSuites();
        assertEquals(1, children.size());
        TestSuiteResult suiteResult = children.get(0);
        TestCaseResult[] cases = suiteResult.getCases().toArray(new TestCaseResult[suiteResult.getCases().size()]);
        assertCases(cases, createPassCase("acase"), createFailureCase("acase2"));
    }

    private TestCaseResult addSuiteAlreadyExistsOverlappingHelper(TestSuiteResult.Resolution resolution)
    {
        TestSuiteResult childSuite = new TestSuiteResult("child suite");
        TestSuiteResult childSuite2 = new TestSuiteResult("child suite");
        TestCaseResult childCase = createPassCase("acase");
        TestCaseResult childCase2 = createFailureCase("acase");
        childSuite.add(childCase, resolution);
        childSuite.add(childCase2, resolution);
        suite.add(childSuite, resolution);
        suite.add(childSuite2, resolution);
        return childCase2;
    }

    private TestCaseResult createPassCase(String name)
    {
        return new TestCaseResult(name, 100, TestCaseResult.Status.PASS, "test message");
    }

    private TestCaseResult createFailureCase(String name)
    {
        return new TestCaseResult(name, 102, TestCaseResult.Status.FAILURE, "failure message");
    }

    private void assertCases(TestCaseResult[] got, TestCaseResult... expected)
    {
        assertEquals(expected.length, got.length);
        for (int i = 0; i < got.length; i++)
        {
            assertTrue(got[i].isEquivalent(expected[i]));
        }
    }
}
