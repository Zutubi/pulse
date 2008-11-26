package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class TestSuiteResult extends TestResult
{
    private final List<TestSuiteResult> suites = new LinkedList<TestSuiteResult>();
    private final List<TestCaseResult> cases = new LinkedList<TestCaseResult>();

    public TestSuiteResult(String name)
    {
        super(name);
    }

    public TestSuiteResult(String name, long duration)
    {
        super(name, duration);
    }

    public List<TestSuiteResult> getSuites()
    {
        return Collections.unmodifiableList(suites);
    }

    public void addSuite(TestSuiteResult suite)
    {
        suites.add(suite);
    }

    public void addAllSuites(Collection<? extends TestSuiteResult> suites)
    {
        this.suites.addAll(suites);
    }

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

    public List<TestCaseResult> getCases()
    {
        return Collections.unmodifiableList(cases);
    }

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

    public void addCase(TestCaseResult caseResult)
    {
        cases.add(caseResult);
    }

    public void addAllCases(Collection<? extends TestCaseResult> cases)
    {
        this.cases.addAll(cases);
    }
}
