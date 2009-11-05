package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.util.*;

import java.io.File;
import java.io.IOException;
import static java.util.Arrays.asList;
import java.util.List;

public class TestReportPostProcessorSupportTest extends TestPostProcessorTestCase
{
    private static final String SUITE_TOP = "top";
    private static final String SUITE_NESTED = "nes/%ed";
    private static final String CASE_PASSED = "passed";
    private static final String CASE_EXPECTED_FAILURE = "expected";
    private static final String CASE_FAILED = "fai/e%";
    private static final String CASE_ERRORED = "errored";
    private static final String CASE_SKIPPED = "skipped";

    private File dummyArtifact;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        dummyArtifact = new File(tempDir, "dummy.txt");
        assertTrue(dummyArtifact.createNewFile());
    }

    public void testExpectedFailureFileDoesNotExist() throws IOException
    {
        assertEquals(buildSuite(null, makeSuite()), runProcessorAndGetTests(new File("nonexistant")));
    }

    public void testExpectedFailureFile() throws IOException
    {
        File failureFile = new File(tempDir, "failures.txt");
        FileSystemUtils.createFile(failureFile,
                StringUtils.join("\n",
                    makeTestPath(SUITE_TOP, CASE_PASSED),
                    makeTestPath(SUITE_TOP, CASE_EXPECTED_FAILURE),
                    makeTestPath(SUITE_TOP, CASE_FAILED),
                    makeTestPath(SUITE_TOP, CASE_ERRORED),
                    makeTestPath(SUITE_TOP, "random case"),
                    makeTestPath(SUITE_TOP, CASE_SKIPPED),
                    makeTestPath("random suite", CASE_FAILED))
        );

        TestSuiteResult testSuiteResult = runProcessorAndGetTests(failureFile);

        assertEquals(buildSuite(null, buildSuite(SUITE_TOP,
                         buildSuite(SUITE_NESTED,
                            new TestCaseResult(CASE_PASSED, TestStatus.PASS),
                            new TestCaseResult(CASE_EXPECTED_FAILURE, TestStatus.EXPECTED_FAILURE),
                            new TestCaseResult(CASE_FAILED, TestStatus.FAILURE),
                            new TestCaseResult(CASE_ERRORED, TestStatus.ERROR),
                            new TestCaseResult(CASE_SKIPPED, TestStatus.SKIPPED)
                         ),
                         new TestCaseResult(CASE_PASSED, TestStatus.PASS),
                         new TestCaseResult(CASE_EXPECTED_FAILURE, TestStatus.EXPECTED_FAILURE),
                         new TestCaseResult(CASE_FAILED, TestStatus.EXPECTED_FAILURE),
                         new TestCaseResult(CASE_ERRORED, TestStatus.EXPECTED_FAILURE),
                         new TestCaseResult(CASE_SKIPPED, TestStatus.SKIPPED)
                     )), testSuiteResult);
    }

    public void testExpectedFailureFileNestedSuite() throws IOException
    {
        File failureFile = new File(tempDir, "failures.txt");
        FileSystemUtils.createFile(failureFile,
                StringUtils.join("\n",
                    makeTestPath(SUITE_TOP, SUITE_NESTED, CASE_PASSED),
                    makeTestPath(SUITE_TOP, SUITE_NESTED, CASE_EXPECTED_FAILURE),
                    makeTestPath(SUITE_TOP, SUITE_NESTED, CASE_FAILED),
                    makeTestPath(SUITE_TOP, SUITE_NESTED, CASE_ERRORED),
                    makeTestPath(SUITE_TOP, SUITE_NESTED, CASE_SKIPPED))
        );

        TestSuiteResult testSuiteResult = runProcessorAndGetTests(failureFile);

        assertEquals(buildSuite(null, buildSuite(SUITE_TOP,
                         buildSuite(SUITE_NESTED,
                            new TestCaseResult(CASE_PASSED, TestStatus.PASS),
                            new TestCaseResult(CASE_EXPECTED_FAILURE, TestStatus.EXPECTED_FAILURE),
                            new TestCaseResult(CASE_FAILED, TestStatus.EXPECTED_FAILURE),
                            new TestCaseResult(CASE_ERRORED, TestStatus.EXPECTED_FAILURE),
                            new TestCaseResult(CASE_SKIPPED, TestStatus.SKIPPED)
                         ),
                         new TestCaseResult(CASE_PASSED, TestStatus.PASS),
                         new TestCaseResult(CASE_EXPECTED_FAILURE, TestStatus.EXPECTED_FAILURE),
                         new TestCaseResult(CASE_FAILED, TestStatus.FAILURE),
                         new TestCaseResult(CASE_ERRORED, TestStatus.ERROR),
                         new TestCaseResult(CASE_SKIPPED, TestStatus.SKIPPED)
                     )), testSuiteResult);
    }

    private TestSuiteResult runProcessorAndGetTests(File failureFile)
    {
        PulseExecutionContext executionContext = new PulseExecutionContext();
        executionContext.setWorkingDir(tempDir);
        TestPostProcessorContext context = new TestPostProcessorContext(executionContext);
        Processor postProcessor = new Processor(new Config(failureFile.getName()), makeSuite());
        postProcessor.process(dummyArtifact, context);
        return context.getTestSuiteResult();
    }

    private String makeTestPath(String... pieces)
    {
        StringBuilder builder = new StringBuilder();
        InvertedPredicate<Character> predicate = new InvertedPredicate<Character>(new InCollectionPredicate<Character>('/', '%'));
        for (String piece: pieces)
        {
            if (builder.length() > 0)
            {
                builder.append('/');
            }

            builder.append(WebUtils.percentEncode(piece, predicate));
        }

        return builder.toString();
    }

    private TestSuiteResult makeSuite()
    {
        return buildSuite(SUITE_TOP,
                   buildSuite(SUITE_NESTED,
                      new TestCaseResult(CASE_PASSED, TestStatus.PASS),
                      new TestCaseResult(CASE_EXPECTED_FAILURE, TestStatus.EXPECTED_FAILURE),
                      new TestCaseResult(CASE_FAILED, TestStatus.FAILURE),
                      new TestCaseResult(CASE_ERRORED, TestStatus.ERROR),
                      new TestCaseResult(CASE_SKIPPED, TestStatus.SKIPPED)
                   ),
                   new TestCaseResult(CASE_PASSED, TestStatus.PASS),
                   new TestCaseResult(CASE_EXPECTED_FAILURE, TestStatus.EXPECTED_FAILURE),
                   new TestCaseResult(CASE_FAILED, TestStatus.FAILURE),
                   new TestCaseResult(CASE_ERRORED, TestStatus.ERROR),
                   new TestCaseResult(CASE_SKIPPED, TestStatus.SKIPPED)
               );
    }

    public static class Config extends TestReportPostProcessorConfigurationSupport
    {
        protected Config(String expectedFailureFile)
        {
            super(Processor.class);
            setExpectedFailureFile(expectedFailureFile);
        }
    }

    public static class Processor extends TestReportPostProcessorSupport
    {
        private List<TestSuiteResult> suites;

        public Processor(Config config, TestSuiteResult... suites)
        {
            super(config);
            this.suites = asList(suites);
        }

        protected void extractTestResults(File file, PostProcessorContext ppContext, TestSuiteResult tests)
        {
            tests.addAllSuites(suites);
        }
    }
}
