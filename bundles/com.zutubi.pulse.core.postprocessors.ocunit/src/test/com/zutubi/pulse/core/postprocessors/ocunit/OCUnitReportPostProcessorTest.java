package com.zutubi.pulse.core.postprocessors.ocunit;

import com.google.common.base.Function;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.PersistentTestSuiteResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.postprocessors.DefaultPostProcessorContext;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;

public class OCUnitReportPostProcessorTest extends PulseTestCase
{
    private File tmpDir = null;
    private StoredFileArtifact artifact = null;
    private CommandResult result = null;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDir("OCUnitReportPostProcessorTest", getName());
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
        IOUtils.joinStreams(
                this.getClass().getResourceAsStream("OCUnitReportPostProcessorTest."+name+".txt"),
                new FileOutputStream(tmpFile),
                true
        );

        return new StoredFileArtifact( name + ".txt");
    }

    public void testEmptySuite()
    {
        PersistentTestSuiteResult tests = process();
        assertEquals(1, tests.getSuites().size());
        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("SenInterfaceTestCase", suite.getName());
        assertEquals(2, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(0, suite.getFailures());
    }

    public void testNestedSuites()
    {
        PersistentTestSuiteResult tests = process();
        assertEquals(1, tests.getSuites().size());
        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("All tests", suite.getName());
        assertEquals(1, suite.getSuites().size());
        assertEquals(2768, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(0, suite.getFailures());

        suite = suite.getSuites().get(0);
        assertEquals("/System/Library/Frameworks/SenTestingKit.framework(Tests)", suite.getName());
        assertEquals(1, suite.getSuites().size());
        assertEquals(30, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(0, suite.getFailures());

        suite = suite.getSuites().get(0);
        assertEquals("SenInterfaceTestCase", suite.getName());
        assertEquals(0, suite.getCases().size());
        assertEquals(4, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(0, suite.getFailures());
    }

    public void testSingleSuiteWithTests()
    {
        assertSingleSuite(process(), 7);
    }

    public void testIncompleteSuite()
    {
        assertSingleSuite(process(), 7);
    }

    public void testMissingSummary()
    {
        assertSingleSuite(process(), 7);
    }

    public void testMismatchedSuiteName()
    {
        assertSingleSuite(process(), 7);
    }

    public void testIncompleteNestedSuites()
    {
        PersistentTestSuiteResult suite = process();
        suite  = assertOneNestedSuite(suite, "All tests");
        suite = assertOneNestedSuite(suite, "/System/Library/Frameworks/SenTestingKit.framework(Tests)");
        assertOneNestedSuite(suite, "SenInterfaceTestCase");
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
        assertEquals("TestCNYieldSorting", suite.getName());
        assertEquals(duration, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(0, suite.getFailures());
        assertEquals(4, suite.getTotal());
    }

    public void testRealSample()
    {
        //Executed 583 tests, with 3 failures (1 unexpected) in 2.768 (3.503) seconds

        PersistentTestSuiteResult tests = process();
        assertEquals(1, tests.getSuites().size());
        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("All tests", suite.getName());
        assertEquals(2768, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(3, suite.getFailures());
        assertEquals(583, suite.getTotal());
    }

    public void testExtendedSampleBuild()
    {
        PersistentTestSuiteResult tests = process();
        assertEquals(1, tests.getSuites().size());
        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("/opt/pulse/pulse-data/recipes/2555916/base/build/Release/UnitTests.octest(Tests)", suite.getName());
        assertEquals(3, suite.getDuration());
        assertEquals(0, suite.getErrors());
        assertEquals(0, suite.getFailures());
        assertEquals(11, suite.getTotal());

    }

    public void testMultipleNestedSuites()
    {
        PersistentTestSuiteResult tests = process();
        assertEquals(1, tests.getSuites().size());
        PersistentTestSuiteResult suite = tests.getSuites().get(0);
        assertEquals("All tests", suite.getName());
        assertEquals(2, suite.getSuites().size());
    }

    public void testShortenSuiteNames()
    {
        if (File.separatorChar != '/')
        {
            // The test data uses forward slash paths.
            return;
        }

        PersistentTestSuiteResult tests = process(true);
        tests = tests.getSuite("All tests");

        List<String> names = transform(tests.getSuites(), new Function<PersistentTestSuiteResult, String>()
        {
            public String apply(PersistentTestSuiteResult persistentTestSuiteResult)
            {
                return persistentTestSuiteResult.getName();
            }
        });

        assertEquals(Arrays.asList("build/Release/AmbUnitTests.octest(Tests)",
                                   "Debug/AmbUnitTests.octest(Tests)",
                                   "foo/Release/AmbUnitTests.octest(Tests)",
                                   "MoreUnitTests.octest(Tests)",
                                   "SenTestingKit.framework(Tests)",
                                   "UnitTests.octest(Tests)"),
                     names);
    }

    private PersistentTestSuiteResult process()
    {
        return process(false);
    }

    private PersistentTestSuiteResult process(boolean shortenSuiteNames)
    {
        OCUnitReportPostProcessorConfiguration config = new OCUnitReportPostProcessorConfiguration();
        config.setShortenSuiteNames(shortenSuiteNames);
        OCUnitReportPostProcessor pp = new OCUnitReportPostProcessor(config);
        PersistentTestSuiteResult testResults = new PersistentTestSuiteResult();

        ExecutionContext context = new PulseExecutionContext();
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_TEST_RESULTS, testResults);
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, tmpDir.getAbsolutePath());
        
        pp.process(new File(tmpDir, artifact.getPath()), new DefaultPostProcessorContext(artifact, result, Integer.MAX_VALUE, context));
        return testResults;
    }
}
