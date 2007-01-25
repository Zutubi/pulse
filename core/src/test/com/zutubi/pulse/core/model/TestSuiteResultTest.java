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
        TestCaseResult childCase = new TestCaseResult("acase", 100, TestCaseResult.Status.PASS, "test message");
        suite.add(childCase);
        assertEquals(1, suite.getTotal());
        assertTrue(suite.getCases().get(0) == childCase);
    }

    public void testAddSuite()
    {
        TestSuiteResult childSuite = new TestSuiteResult("child suite");
        suite.add(childSuite);
        List<TestSuiteResult> children = suite.getSuites();
        assertEquals(1, children.size());
        assertTrue(children.get(0) == childSuite);
    }

    public void testAddCaseAlreadyExists()
    {
        TestCaseResult childCase = new TestCaseResult("acase", 100, TestCaseResult.Status.PASS, "test message");
        TestCaseResult childCase2 = new TestCaseResult("acase", 100, TestCaseResult.Status.PASS, "test message");
        suite.add(childCase);
        suite.add(childCase2);
        
        List<TestCaseResult> childCases = suite.getCases();
        assertEquals(1, childCases.size());
        assertTrue(childCases.get(0).isEquivalent(childCase));
    }

    public void testAddCaseLessSevereAlreadyExists()
    {
        TestCaseResult childCase = new TestCaseResult("acase", 100, TestCaseResult.Status.PASS, "test message");
        TestCaseResult childCase2 = new TestCaseResult("acase", 102, TestCaseResult.Status.FAILURE, "failure message");
        suite.add(childCase);
        suite.add(childCase2);

        List<TestCaseResult> childCases = suite.getCases();
        assertEquals(1, childCases.size());
        assertFalse(childCases.get(0).isEquivalent(childCase));
        assertTrue(childCases.get(0).isEquivalent(childCase2));
    }

    public void testAddCaseMoreSevereAlreadyExists()
    {
        TestCaseResult childCase = new TestCaseResult("acase", 102, TestCaseResult.Status.FAILURE, "failure message");
        TestCaseResult childCase2 = new TestCaseResult("acase", 100, TestCaseResult.Status.PASS, "test message");
        suite.add(childCase);
        suite.add(childCase2);

        List<TestCaseResult> childCases = suite.getCases();
        assertEquals(1, childCases.size());
        assertTrue(childCases.get(0).isEquivalent(childCase));
        assertFalse(childCases.get(0).isEquivalent(childCase2));
    }

    public void testAddSuiteAlreadyExists()
    {
        TestSuiteResult childSuite = new TestSuiteResult("child suite");
        TestSuiteResult childSuite2 = new TestSuiteResult("child suite");
        TestCaseResult childCase = new TestCaseResult("acase", 1002, TestCaseResult.Status.PASS, null);
        TestCaseResult childCase2 = new TestCaseResult("acase2", 102, TestCaseResult.Status.FAILURE, "failure message");
        childSuite.add(childCase);
        childSuite.add(childCase2);
        suite.add(childSuite);
        suite.add(childSuite2);

        List<TestSuiteResult> children = suite.getSuites();
        assertEquals(1, children.size());
        TestSuiteResult suiteResult = children.get(0);
        assertEquals(2, suiteResult.getCases().size());
        assertTrue(suiteResult.getCase(childCase.getName()).isEquivalent(childCase));
        assertTrue(suiteResult.getCase(childCase2.getName()).isEquivalent(childCase2));
    }

    public void testAddSuiteAlreadyExistsOverlappingCase()
    {
        TestSuiteResult childSuite = new TestSuiteResult("child suite");
        TestSuiteResult childSuite2 = new TestSuiteResult("child suite");
        TestCaseResult childCase = new TestCaseResult("acase", 1002, TestCaseResult.Status.PASS, null);
        TestCaseResult childCase2 = new TestCaseResult("acase", 102, TestCaseResult.Status.FAILURE, "failure message");
        childSuite.add(childCase);
        childSuite.add(childCase2);
        suite.add(childSuite);
        suite.add(childSuite2);

        List<TestSuiteResult> children = suite.getSuites();
        assertEquals(1, children.size());
        TestSuiteResult suiteResult = children.get(0);
        assertEquals(1, suiteResult.getCases().size());
        assertTrue(suiteResult.getCases().get(0).isEquivalent(childCase2));
    }
}
