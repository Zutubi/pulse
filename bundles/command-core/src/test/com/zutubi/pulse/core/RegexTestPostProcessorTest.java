package com.zutubi.pulse.core;

import static com.zutubi.pulse.core.BuildProperties.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.model.TestCaseResult;
import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * <class comment/>
 */
public class RegexTestPostProcessorTest extends PulseTestCase
{
    private File tmpDir = null;
    private StoredFileArtifact artifact = null;
    private CommandResult result = null;

    public RegexTestPostProcessorTest()
    {
    }

    public RegexTestPostProcessorTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDir("RegexTestPostProcessorTest", getName());

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
                this.getClass().getResourceAsStream("RegexTestPostProcessorTest."+name+".txt"),
                new FileOutputStream(tmpFile),
                true
        );

        return new StoredFileArtifact( name + ".txt");
    }

    public void testSmokeTest() throws FileLoadException
    {
        TestSuiteResult tests = process();
        assertEquals(5, tests.getFailures());
        assertEquals(91, tests.getTotal());
        assertEquals(0, tests.getErrors());
    }

    public void testConflictsAppend() throws FileLoadException
    {
        TestSuiteResult tests = process("append", false);
        assertEquals(5, tests.getTotal());
        assertTrue(tests.hasCase(" <TEST COMMAND0>"));
        assertTrue(tests.hasCase(" <TEST COMMAND1>"));
        assertTrue(tests.hasCase(" <TEST COMMAND1>2"));
        assertTrue(tests.hasCase(" <TEST COMMAND1>3"));
        assertTrue(tests.hasCase(" <TEST COMMAND2>"));
    }

    public void testConflictsOff() throws FileLoadException
    {
        TestSuiteResult tests = process();
        assertEquals(3, tests.getTotal());
        assertTrue(tests.hasCase(" <TEST COMMAND0>"));
        assertTrue(tests.hasCase(" <TEST COMMAND1>"));
        assertTrue(tests.hasCase(" <TEST COMMAND2>"));
    }

    public void testConflictsPrepend() throws FileLoadException
    {
        TestSuiteResult tests = process("prepend", false);
        assertEquals(5, tests.getTotal());
        assertTrue(tests.hasCase(" <TEST COMMAND0>"));
        assertTrue(tests.hasCase(" <TEST COMMAND1>"));
        assertTrue(tests.hasCase("2 <TEST COMMAND1>"));
        assertTrue(tests.hasCase("3 <TEST COMMAND1>"));
        assertTrue(tests.hasCase(" <TEST COMMAND2>"));
    }

    public void testAutoFail() throws FileLoadException
    {
        TestSuiteResult tests = process("off", true);
        assertEquals(5, tests.getTotal());
        assertEquals(3, tests.getFailures());
        assertEquals(1, tests.getErrors());
        assertEquals(TestCaseResult.Status.PASS, tests.getCase("test1").getStatus());
        assertEquals(TestCaseResult.Status.ERROR, tests.getCase("test2").getStatus());
        assertEquals(TestCaseResult.Status.FAILURE, tests.getCase("test3").getStatus());
        assertEquals(TestCaseResult.Status.FAILURE, tests.getCase("test4").getStatus());
        assertEquals(TestCaseResult.Status.FAILURE, tests.getCase("test5").getStatus());
    }

    public void testUnrecognised() throws FileLoadException
    {
        TestSuiteResult tests = process();
        assertEquals(3, tests.getTotal());
        assertEquals(1, tests.getFailures());
        assertEquals(1, tests.getErrors());
        assertEquals(TestCaseResult.Status.PASS, tests.getCase("test1").getStatus());
        assertEquals(TestCaseResult.Status.ERROR, tests.getCase("test2").getStatus());
        assertEquals(TestCaseResult.Status.FAILURE, tests.getCase("test4").getStatus());
        assertFalse(tests.hasCase("test3"));
        assertFalse(tests.hasCase("test5"));
    }

    private TestSuiteResult process() throws FileLoadException
    {
        return process("off", false);
    }

    private TestSuiteResult process(String resolution, boolean autoFail) throws FileLoadException
    {
        RegexTestPostProcessor pp = new RegexTestPostProcessor();
        pp.setRegex("\\[(.*)\\] .*EDT:(.*)");
        pp.setStatusGroup(1);
        pp.setNameGroup(2);
        pp.setPassStatus("PASS");
        pp.setFailureStatus("FAIL");
        pp.setResolveConflicts(resolution);
        pp.setAutoFail(autoFail);

        TestSuiteResult testResults = new TestSuiteResult();
        ExecutionContext context = new ExecutionContext();
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_TEST_RESULTS, testResults);
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, tmpDir.getAbsolutePath());

        pp.process(artifact, result, context);
        return testResults;
    }
}
