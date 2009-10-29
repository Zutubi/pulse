package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.PulseExecutionContext;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.PersistentTestCaseResult;
import com.zutubi.pulse.core.model.PersistentTestSuiteResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.io.File;

/**
 */
public abstract class XMLTestReportPostProcessorTestBase extends PulseTestCase
{
    protected TestReportPostProcessorSupport pp;

    protected XMLTestReportPostProcessorTestBase(TestReportPostProcessorSupport pp)
    {
        this.pp = pp;
    }

    protected abstract File getOutputDir() throws Exception;

    protected StoredFileArtifact getArtifact(String name)
    {
        return new StoredFileArtifact(getClass().getSimpleName() + "." + name + ".xml");
    }

    protected PersistentTestSuiteResult runProcessor(String... names) throws Exception
    {
        File outputDir = getOutputDir();
        PersistentTestSuiteResult testResults = new PersistentTestSuiteResult();
        ExecutionContext context = new PulseExecutionContext();
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_TEST_RESULTS, testResults);
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, outputDir.getAbsolutePath());

        for(String name: names)
        {
            StoredFileArtifact artifact = getArtifact(name);

            File artifactFile = new File(outputDir.getAbsolutePath(), artifact.getPath());
            // not much point running a test if the artifact being processed does not exist.
            assertTrue("File " + artifactFile.getAbsolutePath() + " does not exist.", artifactFile.exists());

            CommandResult commandResult = new CommandResult("test");
            PostProcessorContext ppContext = new DefaultPostProcessorContext(artifact, commandResult, context);
            pp.process(artifactFile, ppContext);
        }
        
        return testResults;
    }

    protected void checkCase(PersistentTestCaseResult caseResult, String name, TestStatus status, long duration, String message)
    {
        assertEquals(name, caseResult.getName());
        assertEquals(status, caseResult.getStatus());
        assertEquals(duration, caseResult.getDuration());
        assertEquals(message, caseResult.getMessage());
    }

    protected void checkCase(PersistentTestCaseResult caseResult, String name, TestStatus status, String message)
    {
        checkCase(caseResult, name, status, TestResult.DURATION_UNKNOWN, message);
    }

    protected void checkSuite(PersistentTestSuiteResult suite, String name, int total, int failures, int errors)
    {
        assertEquals(name, suite.getName());
        assertEquals(total, suite.getTotal());
        assertEquals(failures, suite.getFailures());
        assertEquals(errors, suite.getErrors());
    }

    protected void checkPassCase(PersistentTestSuiteResult suite, String name)
    {
        PersistentTestCaseResult caseResult = suite.getCase(name);
        assertNotNull(caseResult);
        checkPassCase(caseResult, name);
    }

    protected void checkPassCase(PersistentTestCaseResult caseResult, String name)
    {
        checkCase(caseResult, name, TestStatus.PASS, null);
    }

    protected void checkFailureCase(PersistentTestSuiteResult suite, String name, String message)
    {
        PersistentTestCaseResult caseResult = suite.getCase(name);
        assertNotNull(caseResult);
        checkFailureCase(caseResult, name, message);
    }

    protected void checkFailureCase(PersistentTestCaseResult caseResult, String name, String message)
    {
        checkCase(caseResult, name, TestStatus.FAILURE, message);
    }

    protected void checkErrorCase(PersistentTestSuiteResult suite, String name, String message)
    {
        PersistentTestCaseResult caseResult = suite.getCase(name);
        assertNotNull(caseResult);
        checkErrorCase(caseResult, name, message);
    }

    protected void checkErrorCase(PersistentTestCaseResult caseResult, String name, String message)
    {
        checkCase(caseResult, name, TestStatus.ERROR, message);
    }
}

