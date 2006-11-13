package com.zutubi.pulse.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the results of a test suite: a group of child test suites and
 * cases unified under one name.
 */
public class TestSuiteResult extends TestResult
{
    /**
     * Child suites, stored in name order.
     */
    private List<TestSuiteResult> suites;
    /**
     * Child cases, stored in the order added.
     */
    private List<TestCaseResult> cases;
    private TestResultComparator comparator = new TestResultComparator();

    private int total;
    private int errors;
    private int failures;

    public TestSuiteResult()
    {
        this(null, UNKNOWN_DURATION);
    }

    public TestSuiteResult(String name)
    {
        this(name, UNKNOWN_DURATION);
    }

    public TestSuiteResult(String name, long duration)
    {
        this(name, duration, -1, -1, -1);
        suites = new ArrayList<TestSuiteResult>();
        cases = new ArrayList<TestCaseResult>();
    }

    public TestSuiteResult(String name, long duration, int total, int errors, int failures)
    {
        super(name, duration);
        this.total = total;
        this.errors = errors;
        this.failures = failures;
    }

    public void add(TestSuiteResult suite)
    {
        int index = Collections.binarySearch(suites, suite, comparator);
        if (index < 0)
        {
            suites.add(-index - 1, suite);
        }
        else
        {
            TestSuiteResult existing = suites.get(index);
            for(TestSuiteResult childSuite: suite.getSuites())
            {
                existing.add(childSuite);
            }

            for(TestCaseResult childCase: suite.getCases())
            {
                existing.add(childCase);
            }
        }
    }

    public void add(TestCaseResult childCase)
    {
        cases.add(childCase);
    }

    public int getErrors()
    {
        if (errors < 0)
        {
            int result = 0;

            for (TestSuiteResult child : suites)
            {
                result += child.getErrors();
            }

            for (TestCaseResult child : cases)
            {
                result += child.getErrors();
            }

            return result;
        }
        else
        {
            return errors;
        }
    }

    public int getFailures()
    {
        if (failures < 0)
        {
            int result = 0;

            for (TestSuiteResult child : suites)
            {
                result += child.getFailures();
            }

            for (TestCaseResult child : cases)
            {
                result += child.getFailures();
            }

            return result;
        }
        else
        {
            return failures;
        }
    }

    public int getTotal()
    {
        if (total < 0)
        {
            int result = 0;

            for (TestSuiteResult child : suites)
            {
                result += child.getTotal();
            }

            for (TestCaseResult child : cases)
            {
                result += child.getTotal();
            }

            return result;
        }
        else
        {
            return total;
        }
    }

    public boolean isSuite()
    {
        return true;
    }

    public List<TestSuiteResult> getSuites()
    {
        return suites;
    }

    public List<TestCaseResult> getCases()
    {
        return cases;
    }

    public boolean isEquivalent(TestResult otherResult)
    {
        if(!(otherResult instanceof TestSuiteResult))
        {
            return false;
        }

        TestSuiteResult other = (TestSuiteResult) otherResult;
        if(!super.isEquivalent(other))
        {
            return false;
        }

        List<TestSuiteResult> otherSuites = other.getSuites();
        if(suites.size() != otherSuites.size())
        {
            return false;
        }

        for(int i = 0; i < suites.size(); i++)
        {
            if(!suites.get(i).isEquivalent(otherSuites.get(i)))
            {
                return false;
            }
        }

        List<TestCaseResult> otherCases = other.getCases();
        if(cases.size() != otherCases.size())
        {
            return false;
        }

        for(int i = 0; i < cases.size(); i++)
        {
            if(!cases.get(i).isEquivalent(otherCases.get(i)))
            {
                return false;
            }
        }

        return true;
    }

    public TestSuiteResult getSuite(String name)
    {
        for(TestSuiteResult suite: suites)
        {
            if(suite.getName().equals(name))
            {
                return suite;
            }
        }

        return null;
    }
}
