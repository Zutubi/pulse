package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;
import junit.framework.AssertionFailedError;

import java.util.List;

/**
 * Support base class for XML test-report post-processors.  Identical to
 * {@link com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase}, but
 * uses "xml" as the default extension for test input files.
 *
 * @see com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase
 */
public abstract class XMLTestPostProcessorTestCase extends TestPostProcessorTestCase
{
    public static final String EXTENSION_XML = "xml";

    @Override
    protected String getExtension()
    {
        return EXTENSION_XML;
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
        if (!StringUtils.equals(expected.getName(), actual.getName()))
        {
            return "Suite names differ: '" + expected.getName() + "' != '" + actual.getName() + "'";
        }

        if (expected.getDuration() != actual.getDuration())
        {
            return "Suite durations differ: " + expected.getDuration() + " != " + actual.getDuration();
        }

        TestResultToName<TestCaseResult> caseMapFn = new TestResultToName<TestCaseResult>();
        List<String> expectedCases = CollectionUtils.map(expected.getCases(), caseMapFn);
        List<String> actualCases = CollectionUtils.map(expected.getCases(), caseMapFn);
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
        List<String> expectedSuites = CollectionUtils.map(expected.getSuites(), suiteMapFn);
        List<String> actualSuites = CollectionUtils.map(actual.getSuites(), suiteMapFn);
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

        if (!StringUtils.equals(expectedCase.getMessage(), actualCase.getMessage()))
        {
            message += ": messages differ:\n--------------\n" + expectedCase.getMessage() + "\n----- != -----\n" + actualCase.getMessage() + "\n--------------";
        }

        return message;
    }

    private static class TestResultToName<T extends TestResult> implements Mapping<T, String>
    {
        public String map(T t)
        {
            return t.getName();
        }
    }
}
