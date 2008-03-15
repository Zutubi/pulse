package com.zutubi.pulse.core;

import static com.zutubi.pulse.core.BuildProperties.*;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.test.PulseTestCase;

import java.io.File;

/**
 */
public abstract class XMLReportPostProcessorTestBase extends PulseTestCase
{
    protected XMLReportPostProcessor pp;

    protected XMLReportPostProcessorTestBase(XMLReportPostProcessor pp)
    {
        this.pp = pp;
    }

    protected abstract File getOutputDir();

    protected StoredFileArtifact getArtifact(String name)
    {
        return new StoredFileArtifact(getClass().getSimpleName() + "." + name + ".xml");
    }

    protected TestSuiteResult runProcessor(String... names)
    {
        File outputDir = getOutputDir();
        TestSuiteResult testResults = new TestSuiteResult();
        ExecutionContext context = new ExecutionContext();
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_TEST_RESULTS, testResults);
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, outputDir.getAbsolutePath());

        for(String name: names)
        {
            StoredFileArtifact artifact = getArtifact(name);
            pp.process(artifact, new CommandResult("test"), context);
        }
        
        return testResults;
    }

    protected void checkCase(TestCaseResult caseResult, String name, TestCaseResult.Status status, long duration, String message)
    {
        assertEquals(name, caseResult.getName());
        assertEquals(status, caseResult.getStatus());
        assertEquals(duration, caseResult.getDuration());
        assertEquals(message, caseResult.getMessage());
    }

    protected void checkCase(TestCaseResult caseResult, String name, TestCaseResult.Status status, String message)
    {
        checkCase(caseResult, name, status, TestResult.UNKNOWN_DURATION, message);
    }

    protected void checkSuite(TestSuiteResult suite, String name, int total, int failures, int errors)
    {
        assertEquals(name, suite.getName());
        assertEquals(total, suite.getTotal());
        assertEquals(failures, suite.getFailures());
        assertEquals(errors, suite.getErrors());
    }

    protected void checkPassCase(TestSuiteResult suite, String name)
    {
        TestCaseResult caseResult = suite.getCase(name);
        assertNotNull(caseResult);
        checkPassCase(caseResult, name);
    }

    protected void checkPassCase(TestCaseResult caseResult, String name)
    {
        checkCase(caseResult, name, TestCaseResult.Status.PASS, null);
    }

    protected void checkFailureCase(TestSuiteResult suite, String name, String message)
    {
        TestCaseResult caseResult = suite.getCase(name);
        assertNotNull(caseResult);
        checkFailureCase(caseResult, name, message);
    }

    protected void checkFailureCase(TestCaseResult caseResult, String name, String message)
    {
        checkCase(caseResult, name, TestCaseResult.Status.FAILURE, message);
    }

    protected void checkErrorCase(TestSuiteResult suite, String name, String message)
    {
        TestCaseResult caseResult = suite.getCase(name);
        assertNotNull(caseResult);
        checkErrorCase(caseResult, name, message);
    }

    protected void checkErrorCase(TestCaseResult caseResult, String name, String message)
    {
        checkCase(caseResult, name, TestCaseResult.Status.ERROR, message);
    }
}

