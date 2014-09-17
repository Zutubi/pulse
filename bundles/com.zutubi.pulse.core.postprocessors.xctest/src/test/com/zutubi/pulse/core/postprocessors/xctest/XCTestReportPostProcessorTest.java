package com.zutubi.pulse.core.postprocessors.xctest;

import com.google.common.base.Function;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.PersistentTestCaseResult;
import com.zutubi.pulse.core.model.PersistentTestSuiteResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.postprocessors.DefaultPostProcessorContext;
import com.zutubi.pulse.core.postprocessors.api.TestStatus;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;

public class XCTestReportPostProcessorTest extends PulseTestCase
{
    private File tmpDir = null;
    private StoredFileArtifact artifact = null;
    private CommandResult result = null;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDir("XCTestReportPostProcessorTest", getName());
        artifact = prepareArtifact(this.getName());
        result = new CommandResult("output");
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);

        super.tearDown();
    }

    private StoredFileArtifact prepareArtifact(String name) throws IOException
    {
        File tmpFile = new File(tmpDir, name + ".txt");
        ByteStreams.copy(
                Resources.newInputStreamSupplier(this.getClass().getResource("XCTestReportPostProcessorTest." + name + ".txt")),
                Files.newOutputStreamSupplier(tmpFile)
        );

        return new StoredFileArtifact( name + ".txt");
    }

    public void testEmptySuite()
    {
        PersistentTestSuiteResult tests = process();
        assertEquals(1, tests.getSuites().size());
        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("All tests", suite.getName());
        assertEquals(2, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(0, suite.getFailures());
    }

    public void testSingleSuiteWithTests()
    {
        assertSingleSuite(process(), 2);
    }

    public void testIncompleteSuite()
    {
        assertSingleSuite(process(), 2);
    }

    public void testMissingSummary()
    {
        assertSingleSuite(process(), 2);
    }

    public void testMismatchedSuiteName()
    {
        assertSingleSuite(process(), 2);
    }

    public void testIncompleteNestedSuites()
    {
        PersistentTestSuiteResult suite = process();
        suite  = assertOneNestedSuite(suite, "POZKitTests.xctest");
        assertOneNestedSuite(suite, "NSDataUtilityTest");
    }

    private PersistentTestSuiteResult assertOneNestedSuite(PersistentTestSuiteResult suite, String name)
    {
        assertEquals(0, suite.getCases().size());
        assertEquals(1, suite.getSuites().size());
        suite = suite.getSuites().get(0);
        assertEquals(name, suite.getName());
        return suite;
    }

    private void assertSingleSuite(PersistentTestSuiteResult tests, long duration)
    {
        assertEquals(1, tests.getSuites().size());
        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("NSDataUtilityTest", suite.getName());
        assertEquals(duration, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(0, suite.getFailures());
        assertEquals(3, suite.getTotal());
    }

    public void testRealSample()
    {
        // Executed 147 tests, with 8 failures (1 unexpected) in 3.599 (3.681) seconds. Actual 3 test case failures,
        // but one raises 6 assertion failures.
        PersistentTestSuiteResult tests = process();
        assertEquals(1, tests.getSuites().size());
        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("All tests", suite.getName());
        assertEquals(0, suite.getErrors());
        assertEquals(3, suite.getFailures());
        assertEquals(147, suite.getTotal());
        assertEquals(3599, suite.getDuration());

        // Check we are getting the expected nesting.
        assertEquals(1, suite.getSuites().size());
        suite = suite.getSuites().get(0);
        assertEquals("POZKitTests.xctest", suite.getName());
        assertEquals(11, suite.getSuites().size());

        PersistentTestCaseResult caseResult = suite.getSuite("POZBookmarkTest").getCase("testAddBookmarks");
        assertNotNull(caseResult);
        assertEquals(TestStatus.FAILURE, caseResult.getStatus());
        assertEquals("/Users/jsankey/repo/pozest/POZKit/POZKitTests/POZBookmarkTest.m:69: error: -[POZBookmarkTest testAddBookmarks] : failed: caught \"An error\", \"Bad error\"\n" +
                             "(\n" +
                             "\t0   CoreFoundation                      0x00007fff8e49025c __exceptionPreprocess + 172\n" +
                             "\t1   libobjc.A.dylib                     0x00007fff8eca1e75 objc_exception_throw + 43\n" +
                             "\t2   CoreFoundation                      0x00007fff8e49010c +[NSException raise:format:] + 204\n" +
                             "\t3   POZKitTests                         0x00000001005d3261 -[POZBookmarkTest testAddBookmarks] + 97\n" +
                             "\t4   CoreFoundation                      0x00007fff8e37b9ac __invoking___ + 140\n" +
                             "\t5   CoreFoundation                      0x00007fff8e37b814 -[NSInvocation invoke] + 308\n" +
                             "\t6   XCTest                              0x0000000100679941 -[XCTestCase invokeTest] + 253\n" +
                             "\t7   XCTest                              0x0000000100679b42 -[XCTestCase performTest:] + 150\n" +
                             "\t8   XCTest                              0x0000000100682730 -[XCTest run] + 257\n" +
                             "\t9   XCTest                              0x00000001006788bb -[XCTestSuite performTest:] + 379\n" +
                             "\t10  XCTest                              0x0000000100682730 -[XCTest run] + 257\n" +
                             "\t11  XCTest                              0x00000001006788bb -[XCTestSuite performTest:] + 379\n" +
                             "\t12  XCTest                              0x0000000100682730 -[XCTest run] + 257\n" +
                             "\t13  XCTest                              0x00000001006788bb -[XCTestSuite performTest:] + 379\n" +
                             "\t14  XCTest                              0x0000000100682730 -[XCTest run] + 257\n" +
                             "\t15  XCTest                              0x00000001006758cc __25-[XCTestDriver _runSuite]_block_invoke + 56\n" +
                             "\t16  XCTest                              0x000000010068139d -[XCTestObservationCenter _observeTestExecutionForBlock:] + 162\n" +
                             "\t17  XCTest                              0x0000000100675800 -[XCTestDriver _runSuite] + 269\n" +
                             "\t18  XCTest                              0x00000001006763e9 -[XCTestDriver _checkForTestManager] + 678\n" +
                             "\t19  XCTest                              0x00000001006855d0 +[XCTestProbe runTests:] + 182\n" +
                             "\t20  xctest                              0x0000000100001256 xctest + 4694\n" +
                             "\t21  xctest                              0x00000001000015d6 xctest + 5590\n" +
                             "\t22  xctest                              0x0000000100000ed3 xctest + 3795\n" +
                             "\t23  libdyld.dylib                       0x00007fff8d5735fd start + 1\n" +
                             ")", caseResult.getMessage());

        caseResult = suite.getSuite("POZBookmarkTest").getCase("testDeleteBookmarks");
        assertNotNull(caseResult);
        assertEquals(TestStatus.PASS, caseResult.getStatus());
        assertNull(caseResult.getMessage());
    }

    public void testMultipleNestedSuites()
    {
        PersistentTestSuiteResult tests = process();
        assertEquals(1, tests.getSuites().size());
        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("All tests", suite.getName());
        assertEquals(1, suite.getSuites().size());

        suite = suite.getSuites().get(0);
        assertEquals("POZKitTests.xctest", suite.getName());
        assertEquals(2, suite.getSuites().size());

        assertNotNull(suite.getSuite("NSDataUtilityTest"));
        assertNotNull(suite.getSuite("NSURLUtilityTest"));
    }

    private PersistentTestSuiteResult process()
    {
        return process(false);
    }

    private PersistentTestSuiteResult process(boolean shortenSuiteNames)
    {
        XCTestReportPostProcessorConfiguration config = new XCTestReportPostProcessorConfiguration();
        config.setShortenSuiteNames(shortenSuiteNames);
        XCTestReportPostProcessor pp = new XCTestReportPostProcessor(config);
        PersistentTestSuiteResult testResults = new PersistentTestSuiteResult();

        ExecutionContext context = new PulseExecutionContext();
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_TEST_RESULTS, testResults);
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, tmpDir.getAbsolutePath());
        
        pp.process(new File(tmpDir, artifact.getPath()), new DefaultPostProcessorContext(artifact, result, Integer.MAX_VALUE, context));
        return testResults;
    }
}
