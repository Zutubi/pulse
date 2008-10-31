package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import nu.xom.ParsingException;

import java.io.File;
import java.io.IOException;

/**
 */
public class TestSuitePersisterTest extends PulseTestCase
{
    private File tempDir;
    private TestSuitePersister persister;

    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(TestSuitePersisterTest.class.getName(), "");
        persister = new TestSuitePersister();
    }

    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tempDir);
    }

    public void testEmptySuite() throws Exception
    {
        TestSuiteResult suite = new TestSuiteResult("name", 121);
        roundTrip(suite);
    }

    public void testSingleCase() throws Exception
    {
        TestSuiteResult suite = new TestSuiteResult("suite", 100);
        suite.add(new TestCaseResult("case", 24, TestCaseResult.Status.PASS, "this is the message"));
        roundTrip(suite);
    }

    public void testMultipleCases() throws Exception
    {
        TestSuiteResult suite = new TestSuiteResult("asuite");
        suite.add(new TestCaseResult("case3", -1, TestCaseResult.Status.FAILURE, null));
        suite.add(new TestCaseResult("case1", 24, TestCaseResult.Status.PASS, "this is the message"));
        suite.add(new TestCaseResult("case2", 100, TestCaseResult.Status.ERROR, null));
        roundTrip(suite);
    }

    public void testNestedSuite() throws Exception
    {
        TestSuiteResult suite = new TestSuiteResult("asuite");
        TestSuiteResult nestedSuite = new TestSuiteResult("anestedsuite");
        nestedSuite.add(new TestCaseResult("case1", 100, TestCaseResult.Status.ERROR, null));
        suite.add(new TestCaseResult("case1", -1, TestCaseResult.Status.FAILURE, null));
        suite.add(nestedSuite);
        roundTrip(suite);
    }

    public void testComplexSuite() throws Exception
    {
        TestSuiteResult suite = new TestSuiteResult("asuite");

        TestSuiteResult nestedNestedSuite = new TestSuiteResult("anestednestedsuite");
        nestedNestedSuite.add(new TestCaseResult("deep", 100000, TestCaseResult.Status.FAILURE, "sorry\nit failed"));

        TestSuiteResult nestedSuite = new TestSuiteResult("anestedsuite");
        nestedSuite.add(new TestCaseResult("case1", 100, TestCaseResult.Status.ERROR, null));
        nestedSuite.add(new TestCaseResult("anothercase", 1, TestCaseResult.Status.ERROR, "with a messsage"));
        nestedSuite.add(nestedNestedSuite);
        nestedSuite.add(new TestCaseResult("yetanothercase", 1, TestCaseResult.Status.PASS, "with another messsage"));

        suite.add(new TestCaseResult("case1", -1, TestCaseResult.Status.FAILURE, null));
        suite.add(nestedSuite);
        roundTrip(suite);
    }

    public void testXMLSpecialCharacters() throws Exception
    {
        TestSuiteResult suite = new TestSuiteResult("<>&'\"!<!-- -->");
        suite.add(new TestCaseResult("<>&'\"!<!-- -->", 1, TestCaseResult.Status.PASS, "<>&'\"!<!-- -->"));

        TestSuiteResult outer = new TestSuiteResult("outer");
        outer.add(suite);
        roundTrip(outer);
    }

    public void testControlCharacters() throws Exception
    {
        TestSuiteResult suite = new TestSuiteResult("\u0018");
        suite.add(new TestCaseResult("\u0018", 1, TestCaseResult.Status.PASS, "\u0018"));

        TestSuiteResult outer = new TestSuiteResult("outer");
        outer.add(suite);
        roundTrip(outer);
    }

    public void testEmptyMessage() throws Exception
    {
        TestSuiteResult suite = new TestSuiteResult("suity");
        suite.add(new TestCaseResult("castor", 1, TestCaseResult.Status.PASS, ""));

        TestSuiteResult outer = new TestSuiteResult("outer");
        outer.add(suite);
        roundTrip(outer);
    }

    private void roundTrip(TestSuiteResult suite) throws IOException, ParsingException
    {
        persister.write(suite, tempDir);
        TestSuiteResult otherSuite = persister.read(suite.getName(), tempDir, true, false, -1);
        assertTrue(suite.isEquivalent(otherSuite));
    }
}
