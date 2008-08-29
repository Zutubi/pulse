package com.zutubi.pulse.core.model;

import java.util.*;

/**
 * Represents the results of a test suite: a group of child test suites and
 * cases unified under one name.
 */
public class TestSuiteResult extends TestResult
{
    public enum Resolution
    {
        APPEND,
        OFF,
        PREPEND
    }

    /**
     * Child suites, stored in name order.
     */
    private List<TestSuiteResult> suites;
    /**
     * Child cases, stored in the order added.
     */
    private LinkedHashMap<String, TestCaseResult> cases;
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
        cases = new LinkedHashMap<String, TestCaseResult>();
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
        add(suite, Resolution.OFF);
    }
    
    public void add(TestSuiteResult suite, Resolution resolution)
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
                existing.add(childSuite, resolution);
            }

            for(TestCaseResult childCase: suite.getCases())
            {
                existing.add(childCase, resolution);
            }
        }
    }

    public void add(TestCaseResult childCase)
    {
        add(childCase, Resolution.OFF);
    }
    
    public void add(TestCaseResult childCase, Resolution resolution)
    {
        TestCaseResult existing = getCase(childCase.getName());
        if (existing == null)
        {
            addCase(childCase);
        }
        else
        {
            if(resolution == Resolution.OFF)
            {
                if(childCase.getStatus().compareTo(existing.getStatus()) > 0)
                {
                    // The new is more severe.  Although we can't keep all info,
                    // be nice and keep the worst result.
                    addCase(childCase);
                }
            }
            else
            {
                int addition = 2;
                while(hasCase(makeCaseName(childCase.getName(), addition, resolution)))
                {
                    addition++;
                }

                childCase.setName(makeCaseName(childCase.getName(), addition, resolution));
                addCase(childCase);
            }
        }
    }

    private void addCase(TestCaseResult childCase)
    {
        cases.put(childCase.getName(), childCase);
    }

    private String makeCaseName(String name, int addition, Resolution resolveConflicts)
    {
        if(resolveConflicts == Resolution.APPEND)
        {
            return name + addition;
        }
        else
        {
            return Integer.toString(addition) + name;
        }
    }

    public TestCaseResult getCase(String name)
    {
        return cases.get(name);
    }

    public boolean hasCase(String name)
    {
        return getCase(name) != null;
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

            for (TestCaseResult child : cases.values())
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

            for (TestCaseResult child : cases.values())
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

            for (TestCaseResult child : cases.values())
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

    public Collection<TestCaseResult> getCases()
    {
        return cases.values();
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

        Collection<TestCaseResult> otherCases = other.getCases();
        if(cases.size() != otherCases.size())
        {
            return false;
        }

        Iterator<TestCaseResult> ourIt = cases.values().iterator();
        Iterator<TestCaseResult> otherIt = otherCases.iterator();
        while(ourIt.hasNext())
        {
            if(!ourIt.next().isEquivalent(otherIt.next()))
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
