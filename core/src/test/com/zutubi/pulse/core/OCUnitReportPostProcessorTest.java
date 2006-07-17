package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.model.TestResult;
import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * <class-comment/>
 */
public class OCUnitReportPostProcessorTest extends PulseTestCase
{
    private File tmpDir = null;
    private StoredFileArtifact artifact = null;
    private CommandResult result = null;

    public OCUnitReportPostProcessorTest()
    {
    }

    public OCUnitReportPostProcessorTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDirectory("xcode-test-pp-test", null);

        artifact = prepareArtifact(this.getName());

        result = new CommandResult("output");
    }

    protected void tearDown() throws Exception
    {
        artifact = null;
        removeDirectory(tmpDir);

        super.tearDown();
    }

    private StoredFileArtifact prepareArtifact(String name) throws IOException
    {
        File tmpFile = new File(tmpDir, name + ".txt");
        IOUtils.joinStreams(
                this.getClass().getResourceAsStream("OCUnitReportPostProcessorTest."+name+".txt"),
                new FileOutputStream(tmpFile),
                true
        );

        return new StoredFileArtifact( name + ".txt");
    }

    public void testEmptySuite()
    {
        OCUnitReportPostProcessor pp = new OCUnitReportPostProcessor();
        pp.process(tmpDir, artifact, result);

        List<TestResult> tests = artifact.getTests();
        assertEquals(1, tests.size());
        TestSuiteResult suite = (TestSuiteResult) tests.get(0);
        assertEquals("SenInterfaceTestCase", suite.getName());
        assertEquals(2, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(0, suite.getFailures());
    }

    public void testNestedSuites()
    {
        OCUnitReportPostProcessor pp = new OCUnitReportPostProcessor();
        pp.process(tmpDir, artifact, result);

        List<TestResult> tests = artifact.getTests();
        assertEquals(1, tests.size());
        TestSuiteResult suite = (TestSuiteResult) tests.get(0);
        assertEquals("All tests", suite.getName());
        assertEquals(1, suite.getChildren().size());
        assertEquals(2768, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(0, suite.getFailures());

        suite = (TestSuiteResult) suite.getChildren().get(0);
        assertEquals("/System/Library/Frameworks/SenTestingKit.framework(Tests)", suite.getName());
        assertEquals(1, suite.getChildren().size());
        assertEquals(30, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(0, suite.getFailures());

        suite = (TestSuiteResult) suite.getChildren().get(0);
        assertEquals("SenInterfaceTestCase", suite.getName());
        assertEquals(0, suite.getChildren().size());
        assertEquals(4, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(0, suite.getFailures());
    }

    public void testSingleSuiteWithTests()
    {
        OCUnitReportPostProcessor pp = new OCUnitReportPostProcessor();
        pp.process(tmpDir, artifact, result);

        List<TestResult> tests = artifact.getTests();
        assertEquals(1, tests.size());
        TestSuiteResult suite = (TestSuiteResult) tests.get(0);
        assertEquals("TestCNYieldSorting", suite.getName());
        assertEquals(7, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(0, suite.getFailures());
        assertEquals(4, suite.getTotal());
    }

    public void testRealSample()
    {
        OCUnitReportPostProcessor pp = new OCUnitReportPostProcessor();
        pp.process(tmpDir, artifact, result);

        //Executed 583 tests, with 3 failures (1 unexpected) in 2.768 (3.503) seconds

        List<TestResult> tests = artifact.getTests();
        assertEquals(1, tests.size());
        TestSuiteResult suite = (TestSuiteResult) tests.get(0);
        assertEquals("All tests", suite.getName());
        assertEquals(2768, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(3, suite.getFailures());
        assertEquals(583, suite.getTotal());
    }

    public void testMultipleNestedSuites()
    {
        OCUnitReportPostProcessor pp = new OCUnitReportPostProcessor();
        pp.process(tmpDir, artifact, result);

        List<TestResult> tests = artifact.getTests();
        assertEquals(1, tests.size());
        TestSuiteResult suite = (TestSuiteResult) tests.get(0);
        assertEquals("All tests", suite.getName());
        assertEquals(2, suite.getChildren().size());
    }
}
