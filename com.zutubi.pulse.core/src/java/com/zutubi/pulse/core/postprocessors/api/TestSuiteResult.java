package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the result of executing a test suite.  Test suites are
 * hierarchical collections of test results.  They may include both nested
 * suites and test cases.
 *
 * @see com.zutubi.pulse.core.postprocessors.api.TestCaseResult
 */
public class TestSuiteResult extends TestResult
{
    private final List<TestSuiteResult> suites = new LinkedList<TestSuiteResult>();
    private final List<TestCaseResult> cases = new LinkedList<TestCaseResult>();

    /**
     * Creates a suite with the given name and an unknown duration.
     *
     * @param name the name of the suite
     */
    public TestSuiteResult(String name)
    {
        super(name);
    }

    /**
     * Creates a suite with the given name and duration.
     *
     * @param name     name of the suite
     * @param duration time it took to execute the suite, in milliseconds
     */
    public TestSuiteResult(String name, long duration)
    {
        super(name, duration);
    }

    /**
     * @return an unmodifiable list of all directly-nested suites
     */
    public List<TestSuiteResult> getSuites()
    {
        return Collections.unmodifiableList(suites);
    }

    /**
     * Adds the given suite to our nested suites.
     *
     * @param suite the suite to add
     */
    public void addSuite(TestSuiteResult suite)
    {
        suites.add(suite);
    }

    /**
     * Adds all suites in the given collection to our nested suites.
     *
     * @param suites the suites to add
     */
    public void addAllSuites(Collection<? extends TestSuiteResult> suites)
    {
        this.suites.addAll(suites);
    }

    /**
     * Finds a nested suite by name.  Only directly-nested suites are searched.
     *
     * @param name the name to search for
     * @return the nested suite with the given name, or null if not found
     */
    public TestSuiteResult findSuite(final String name)
    {
        return CollectionUtils.find(suites, new Predicate<TestSuiteResult>()
        {
            public boolean satisfied(TestSuiteResult suiteResult)
            {
                return suiteResult.getName().equals(name);
            }
        });
    }

    /**
     * @return an unmodifiable list of all directly-nested test cases
     */
    public List<TestCaseResult> getCases()
    {
        return Collections.unmodifiableList(cases);
    }

    /**
     * Finds a case by name.  Only direcly-nested cases are searched.
     *
     * @param name the name to search for
     * @return the case with the given name, or null if not found
     */
    public TestCaseResult findCase(final String name)
    {
        return CollectionUtils.find(cases, new Predicate<TestCaseResult>()
        {
            public boolean satisfied(TestCaseResult caseResult)
            {
                return caseResult.getName().equals(name);
            }
        });
    }

    /**
     * Adds the given case to this suite.
     *
     * @param caseResult the case to add
     */
    public void addCase(TestCaseResult caseResult)
    {
        cases.add(caseResult);
    }

    /**
     * Adds all cases in the given collection to this suite.
     *
     * @param cases the cases to add
     */
    public void addAllCases(Collection<? extends TestCaseResult> cases)
    {
        this.cases.addAll(cases);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        TestSuiteResult that = (TestSuiteResult) o;

        if (!cases.equals(that.cases))
        {
            return false;
        }
        if (!suites.equals(that.suites))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + suites.hashCode();
        result = 31 * result + cases.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("TestSuite(name: ");
        builder.append(getName());
        builder.append(", duration: ");
        builder.append(getDuration());
        builder.append(")\n");

        for (TestSuiteResult suite: suites)
        {
            appendObject(builder, suite);
        }

        for (TestCaseResult caseResult: cases.toArray(new TestCaseResult[cases.size()]))
        {
            appendObject(builder, caseResult);
        }

        return builder.toString();
    }

    private void appendObject(StringBuilder builder, Object o)
    {
        BufferedReader r = new BufferedReader(new StringReader(o.toString()));
        String line;
        try
            {
                while ((line = r.readLine()) != null)
            {
                builder.append("  ");
                builder.append(line);
                builder.append("\n");
            }
        }
        catch (IOException e)
        {
            // Never happens
        }
    }
}
