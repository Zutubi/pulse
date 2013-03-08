package com.zutubi.pulse.core.postprocessors.api;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import junit.framework.AssertionFailedError;

import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.*;

/**
 * Support base class for test report post-processor test cases.  This case
 * provides convenience methods for running a processor and getting the test
 * results, and for constructing test suites for comparison with the returned
 * results.  A typical test case is expected to look like:
 * <pre>{@code
 *public void testBasic()
 *{
 *    TestSuiteResult expected =
 *        buildSuite(null
 *            buildSuite("FirstSuite",
 *                new TestCaseResult("FirstCase", PASS),
 *                new TestCaseResult("SecondCase", PASS)
 *            ),
 *            buildSuite("SecondSuite",
 *                // ...
 *            ),
 *            new TestCaseResult("Orphaned", 12, FAILURE, "Failure message")
 *        );
 *    assertEquals(expected, runProcessorAndGetTests(new FooTestPostProcessor()));
 *}}
 * </pre>
 * Assuming the test class is named FooTestPostProcessorTest, the input file
 * used would be FooTestPostProcessorTest.testBasic.txt, located in the same
 * package on the classpath.
 *
 * @see com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase
 * @see com.zutubi.pulse.core.postprocessors.api.XMLTestPostProcessorTestCase
 */
public abstract class TestPostProcessorTestCase extends PostProcessorTestCase
{
    /**
     * Equivalent to runProcessorAndGetTests(postProcessor, getExtension()).
     * Runs the processor using the default name (the name of the test method)
     * and extension (from {@link #getExtension()}) to locate the input file.
     *
     * @see #runProcessor(PostProcessor, String)
     * @see #runProcessor(PostProcessor, String, String)
     *
     * @param postProcessor the post-processor to run
     * @return test results found by the post-processor in the input file
     * @throws IOException on any error
     */
    public TestSuiteResult runProcessorAndGetTests(PostProcessor postProcessor) throws IOException
    {
        return runProcessorAndGetTests(postProcessor, getExtension());
    }

    /**
     * Equivalent to runProcessorAndGetTests(postProcessor, getName(), extension).
     * Runs the processor using the default name (the name of the test method)
     * to locate the input file.
     *
     * @see #runProcessor(PostProcessor)
     * @see #runProcessor(PostProcessor, String, String)
     *
     * @param postProcessor the post-processor to run
     * @param extension     the extension of the test input file
     * @return test results found by the post-processor in the input file
     * @throws IOException on any error
     */
    public TestSuiteResult runProcessorAndGetTests(PostProcessor postProcessor, String extension) throws IOException
    {
        return runProcessorAndGetTests(postProcessor, getName(), extension);
    }

    /**
     * Runs the given post-processor over the test input file with the given name
     * and extension, returning the base test suite found.  Note that this suite
     * will have a null name and all found suites and cases will be nested
     * within it.
     * <p/>
     * The processor is run using {@link com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase#runProcessor(PostProcessor, String, String)},
     * thus the input file for post-processing should be located alongside the
     * test class in the classpath.
     *
     * @see #runProcessor(PostProcessor)
     * @see #runProcessor(PostProcessor, String)
     * @see com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase#runProcessor(PostProcessor, String, String)
     * @see com.zutubi.pulse.core.test.api.PulseTestCase#copyInputToDirectory(String, java.io.File)
     *
     * @param postProcessor the post-processor to run
     * @param name          the name of the test input file
     * @param extension     the extension of the test input file
     * @return test results found by the post-processor in the input file
     * @throws IOException on any error
     */
    public TestSuiteResult runProcessorAndGetTests(PostProcessor postProcessor, String  name, String extension) throws IOException
    {
        if (postProcessor instanceof TestReportPostProcessorSupport)
        {
            ((TestReportPostProcessorSupport) postProcessor).enableTestMode();
        }
        TestPostProcessorContext context = runProcessor(postProcessor, name, extension);
        return context.getTestSuiteResult();
    }

    /**
     * Equivalent to buildSuite(name, TestResult.DURATION_UNKNOWN, nested).
     * Builds a test suite with a default (unknown) duration.
     *
     * @param name   name of the test suite to create (may be null)
     * @param nested nested suites and cases to be added to the suite
     * @return the created test suite
     */
    public static TestSuiteResult buildSuite(String name, TestResult... nested)
    {
        return buildSuite(name, TestResult.DURATION_UNKNOWN, nested);
    }

    /**
     * Convenience method to create a test suite result with the given name,
     * duration and nested suites and cases.  This allows a tree of test
     * results to be built up using a single expression, for comparison with
     * results returned by a post-processor.
     *
     * @param name     name of the test suite to create (may be null)
     * @param duration duration in milliseconds that the suite ran for
     * @param nested   nested suites and cases to be added to the suite
     * @return the created test suite
     */
    public static TestSuiteResult buildSuite(String name, long duration, TestResult... nested)
    {
        TestSuiteResult result = new TestSuiteResult(name, duration);
        addAll(result, nested);
        return result;
    }

    /**
     * Adds the given results to the given suite, by determining if they are
     * suites or cases and adding them appropriately.
     *
     * @param suite  suite to add the nested results to
     * @param nested nested results added to the suite
     */
    public static void addAll(TestSuiteResult suite, TestResult... nested)
    {
        for (TestResult r: nested)
        {
            if (r instanceof TestSuiteResult)
            {
                suite.addSuite((TestSuiteResult) r);
            }
            else
            {
                suite.addCase((TestCaseResult) r);
            }
        }
    }

    /**
     * Specialisation of equals assertion for test suites that provides more
     * useful information when suites differ.  The first significant difference
     * is found and reported rather than just a dump of two large suites.
     *
     * @param expected the test suite expected by the test
     * @param actual   the actual test suite seen by the test
     */
    public static void assertEquals(TestSuiteResult expected, TestSuiteResult actual)
    {
        if (!expected.equals(actual))
        {
            throw new AssertionFailedError("Test suites not equivalent: First difference: " + getFirstDifference(expected, actual) + "\n\nFull suites: expected: <" + expected + "> but was:<" + actual + ">");
        }
    }

    private static String getFirstDifference(TestSuiteResult expected, TestSuiteResult actual)
    {
        if (!Objects.equal(expected.getName(), actual.getName()))
        {
            return "Suite names differ: '" + expected.getName() + "' != '" + actual.getName() + "'";
        }

        if (expected.getDuration() != actual.getDuration())
        {
            return "Suite durations differ: " + expected.getDuration() + " != " + actual.getDuration();
        }

        TestResultToName<TestCaseResult> caseMapFn = new TestResultToName<TestCaseResult>();
        List<String> expectedCases = newArrayList(transform(expected.getCases(), caseMapFn));
        List<String> actualCases = newArrayList(transform(actual.getCases(), caseMapFn));
        if (!expectedCases.equals(actualCases))
        {
            return "Nested case names differ: " + expectedCases + " != " + actualCases;
        }

        for (TestCaseResult expectedCase: expected.getCases())
        {
            TestCaseResult actualCase = actual.findCase(expectedCase.getName());
            if (!expectedCase.equals(actualCase))
            {
                return getFirstDifference(expectedCase, actualCase);
            }
        }

        TestResultToName<TestSuiteResult> suiteMapFn = new TestResultToName<TestSuiteResult>();
        List<String> expectedSuites = newArrayList(transform(expected.getSuites(), suiteMapFn));
        List<String> actualSuites = newArrayList(transform(actual.getSuites(), suiteMapFn));
        if (!expectedSuites.equals(actualSuites))
        {
            return "Nested suite names differ: " + expectedSuites + " != " + actualSuites;
        }

        for (TestSuiteResult expectedSuite: expected.getSuites())
        {
            TestSuiteResult actualSuite = actual.findSuite(expectedSuite.getName());
            if (!expectedSuite.equals(actualSuite))
            {
                return "Nested suite '" + expectedSuite.getName() + "': " + getFirstDifference(expectedSuite, actualSuite);
            }
        }

        return "Unable to determine difference";
    }

    private static String getFirstDifference(TestCaseResult expectedCase, TestCaseResult actualCase)
    {
        String message = "Case '" + expectedCase.getName() + "'";
        if (expectedCase.getStatus() != actualCase.getStatus())
        {
            message += ": statuses differ: " + expectedCase.getStatus() + " != " + actualCase.getStatus();
        }

        if (expectedCase.getDuration() != actualCase.getDuration())
        {
            message += ": durations differ: " + expectedCase.getDuration() + " != " + actualCase.getDuration();
        }

        if (!Objects.equal(expectedCase.getMessage(), actualCase.getMessage()))
        {
            message += ": messages differ:\n--------------\n" + expectedCase.getMessage() + "\n----- != -----\n" + actualCase.getMessage() + "\n--------------";
        }

        return message;
    }

    /**
     * Asserts the counts of cases with each status within a suite.
     *
     * @param suite            the suite to test
     * @param name             expected name of the suite
     * @param total            expected total number of nested cases
     * @param failures         expected total number of nested failures
     * @param errors           expected total number of nested errors
     * @param expectedFailures expected total number of nested expected
     *                         failures
     * @param skips            expected total number of nested skips
     */
    protected void checkStatusCounts(TestSuiteResult suite, String name, int total, int failures, int errors, int expectedFailures, int skips)
    {
        assertEquals(name, suite.getName());
        assertEquals(total, suite.getTotal());
        assertEquals(failures, suite.getTotalWithStatus(FAILURE));
        assertEquals(errors, suite.getTotalWithStatus(ERROR));
        assertEquals(expectedFailures, suite.getTotalWithStatus(EXPECTED_FAILURE));
        assertEquals(skips, suite.getTotalWithStatus(SKIPPED));
    }

    private static class TestResultToName<T extends TestResult> implements Function<T, String>
    {
        public String apply(T t)
        {
            return t.getName();
        }
    }
}
