package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.util.InCollectionPredicate;
import com.zutubi.util.InvertedPredicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;

public class TestReportPostProcessorSupportTest extends TestPostProcessorTestCase
{
    private static final String SUITE_TOP = "top";
    private static final String SUITE_NESTED = "nes/%ed";
    private static final String SUITE_WRAPPING = "wrapping";
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
        Processor processor = new Processor(new Config(new File("nonexistant").getName()), makeSuite());
        assertEquals(buildSuite(null, makeSuite()), runProcessorAndGetTests(processor));
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

        Config config = new Config(failureFile.getName());
        TestSuiteResult testSuiteResult = runProcessorAndGetTests(new Processor(config, makeSuite()));

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

        Config config = new Config(failureFile.getName());
        TestSuiteResult testSuiteResult = runProcessorAndGetTests(new Processor(config, makeSuite()));

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

    public void testExpectedFailureFileWrappingSuite() throws IOException
    {
        File failureFile = new File(tempDir, "failures.txt");
        FileSystemUtils.createFile(failureFile,
                                   StringUtils.join("\n",
                                                    makeTestPath(SUITE_WRAPPING, SUITE_TOP, CASE_PASSED),
                                                    makeTestPath(SUITE_WRAPPING, SUITE_TOP, CASE_EXPECTED_FAILURE),
                                                    makeTestPath(SUITE_WRAPPING, SUITE_TOP, CASE_FAILED),
                                                    makeTestPath(SUITE_WRAPPING, SUITE_TOP, CASE_ERRORED),
                                                    makeTestPath(SUITE_WRAPPING, SUITE_TOP, CASE_SKIPPED))
        );

        Config config = new Config(failureFile.getName());
        config.setSuite(SUITE_WRAPPING);
        TestSuiteResult testSuiteResult = runProcessorAndGetTests(new Processor(config, makeSuite()));

        assertEquals(buildSuite(null, buildSuite(SUITE_WRAPPING,
                buildSuite(SUITE_TOP,
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
                ))), testSuiteResult);
    }

    public void testAccumulationWhenChildrenHaveNoDurations()
    {
        TestSuiteResult rawResult = buildSuite("top",
                buildSuite("child",
                        new TestCaseResult(CASE_PASSED, TestStatus.PASS),
                        new TestCaseResult(CASE_PASSED, TestStatus.PASS),
                        new TestCaseResult(CASE_PASSED, TestStatus.PASS)
                )
        );

        TestSuiteResult processedResult = runProcessorAndGetTests(new Processor(new Config(null), rawResult));

        assertEquals(buildSuite(null,
                buildSuite("top",
                        buildSuite("child",
                                new TestCaseResult(CASE_PASSED, TestStatus.PASS),
                                new TestCaseResult(CASE_PASSED, TestStatus.PASS),
                                new TestCaseResult(CASE_PASSED, TestStatus.PASS)
                        )
                )), processedResult);
    }

    public void testAccumulationOfChildrenIntoSuite()
    {
        TestSuiteResult rawResult = buildSuite("top",
                buildSuite("child",
                        new TestCaseResult(CASE_PASSED, 1, TestStatus.PASS),
                        new TestCaseResult(CASE_PASSED, 2, TestStatus.PASS),
                        new TestCaseResult(CASE_PASSED, 3, TestStatus.PASS)
                )
        );

        TestSuiteResult processedResult = runProcessorAndGetTests(new Processor(new Config(null), rawResult));

        assertEquals(buildSuite(null,
                buildSuite("top", 6,
                        buildSuite("child", 6,
                                new TestCaseResult(CASE_PASSED, 1, TestStatus.PASS),
                                new TestCaseResult(CASE_PASSED, 2, TestStatus.PASS),
                                new TestCaseResult(CASE_PASSED, 3, TestStatus.PASS)
                        )
                )), processedResult);
    }

    public void testAccumulationHaltsAtKnownDuration()
    {
        TestSuiteResult rawResult = buildSuite("top",
                buildSuite("child", 1,
                        buildSuite("grandchild",
                                new TestCaseResult(CASE_SKIPPED, 3, TestStatus.SKIPPED)
                        )
                )
        );

        TestSuiteResult processedResult = runProcessorAndGetTests(new Processor(new Config(null), rawResult));

        assertEquals(buildSuite(null,
                buildSuite("top", 1,
                        buildSuite("child", 1,
                                buildSuite("grandchild",
                                        new TestCaseResult(CASE_SKIPPED, 3, TestStatus.SKIPPED)
                                )
                        )
                )), processedResult);
    }

    public void testAccumulationDoesNotAddUnknownDurations()
    {
        TestSuiteResult rawResult = buildSuite("top",
                buildSuite("childA", 1),
                buildSuite("childB"),
                new TestCaseResult(CASE_PASSED, 1, TestStatus.PASS),
                new TestCaseResult(CASE_PASSED, TestStatus.PASS)
        );

        TestSuiteResult processedResult = runProcessorAndGetTests(new Processor(new Config(null), rawResult));

        assertEquals(buildSuite(null,
                buildSuite("top", 2,
                        buildSuite("childA", 1),
                        buildSuite("childB"),
                        new TestCaseResult(CASE_PASSED, 1, TestStatus.PASS),
                        new TestCaseResult(CASE_PASSED, TestStatus.PASS)
                )), processedResult);
    }

    private TestSuiteResult runProcessorAndGetTests(Processor processor)
    {
        PulseExecutionContext executionContext = new PulseExecutionContext();
        executionContext.setWorkingDir(tempDir);
        TestPostProcessorContext context = new TestPostProcessorContext(executionContext);
        processor.process(dummyArtifact, context);
        return context.getTestSuiteResult();
    }

    private String makeTestPath(String... pieces)
    {
        StringBuilder builder = new StringBuilder();
        InvertedPredicate<Character> predicate = new InvertedPredicate<Character>(new InCollectionPredicate<Character>('/', '%'));
        for (String piece : pieces)
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
