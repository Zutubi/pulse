package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.postprocessors.api.NameConflictResolution;
import com.zutubi.pulse.core.postprocessors.api.TestCaseResult;
import com.zutubi.pulse.core.postprocessors.api.TestResult;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;

import java.util.*;

/**
 * Represents the results of a test suite: a group of child test suites and
 * cases unified under one name.
 */
public class PersistentTestSuiteResult extends PersistentTestResult
{
    /**
     * Child suites, stored in name order.
     */
    private List<PersistentTestSuiteResult> suites;
    /**
     * Child cases, stored in the order added.
     */
    private LinkedHashMap<String, PersistentTestCaseResult> cases;
    private TestResultComparator comparator = new TestResultComparator();

    private NameConflictResolution conflictResolution = NameConflictResolution.WORST_RESULT;

    private int total;
    private int expectedFailures;
    private int errors;
    private int failures;
    private int skipped;

    public PersistentTestSuiteResult()
    {
        this(null, TestResult.DURATION_UNKNOWN);
    }

    public PersistentTestSuiteResult(String name)
    {
        this(name, TestResult.DURATION_UNKNOWN);
    }

    public PersistentTestSuiteResult(String name, long duration)
    {
        this(name, duration, -1, -1, -1, -1, -1);
        suites = new ArrayList<PersistentTestSuiteResult>();
        cases = new LinkedHashMap<String, PersistentTestCaseResult>();
    }

    public PersistentTestSuiteResult(TestSuiteResult suite, NameConflictResolution resolution)
    {
        this(suite.getName(), suite.getDuration());
        this.conflictResolution = resolution;
        for (TestSuiteResult nestedSuite : suite.getSuites())
        {
            add(new PersistentTestSuiteResult(nestedSuite, resolution));
        }

        for (TestCaseResult caseResult : suite.getCases())
        {
            add(convertCase(caseResult));
        }
    }

    public PersistentTestSuiteResult(String name, long duration, int total, int expectedFailures, int errors, int failures, int skipped)
    {
        super(name, duration);
        this.total = total;
        this.expectedFailures = expectedFailures;
        this.errors = errors;
        this.failures = failures;
        this.skipped = skipped;
    }

    public void add(PersistentTestSuiteResult suite)
    {
        int index = Collections.binarySearch(suites, suite, comparator);
        if (index < 0)
        {
            suites.add(-index - 1, suite);
        }
        else
        {
            PersistentTestSuiteResult existing = suites.get(index);
            for(PersistentTestSuiteResult childSuite: suite.getSuites())
            {
                existing.add(childSuite);
            }

            for(PersistentTestCaseResult childCase: suite.getCases())
            {
                existing.add(childCase);
            }
        }
    }

    public void add(PersistentTestCaseResult childCase)
    {
        PersistentTestCaseResult existing = getCase(childCase.getName());
        if (existing == null)
        {
            cases.put(childCase.getName(), childCase);
        }
        else
        {
            if (shouldReplace(childCase, existing))
            {
                cases.put(childCase.getName(), childCase);
            }
        }
    }

    private boolean shouldReplace(PersistentTestCaseResult newCase, PersistentTestCaseResult existingCase)
    {
        switch (conflictResolution)
        {
            case WORST_RESULT:
                return newCase.getStatus().compareTo(existingCase.getStatus()) > 0;

            case BEST_RESULT:
                return newCase.getStatus().compareTo(existingCase.getStatus()) < 0;

            case LAST_RESULT:
                return true;

            default:
                return false;
        }
    }

    public PersistentTestCaseResult getCase(String name)
    {
        return cases.get(name);
    }

    public boolean hasCase(String name)
    {
        return getCase(name) != null;
    }

    @Override
    public int getExpectedFailures()
    {
        if (expectedFailures < 0)
        {
            int result = 0;

            for (PersistentTestSuiteResult child : suites)
            {
                result += child.getExpectedFailures();
            }

            for (PersistentTestCaseResult child : cases.values())
            {
                result += child.getExpectedFailures();
            }

            return result;
        }
        else
        {
            return expectedFailures;
        }
    }

    public int getErrors()
    {
        if (errors < 0)
        {
            int result = 0;

            for (PersistentTestSuiteResult child : suites)
            {
                result += child.getErrors();
            }

            for (PersistentTestCaseResult child : cases.values())
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

            for (PersistentTestSuiteResult child : suites)
            {
                result += child.getFailures();
            }

            for (PersistentTestCaseResult child : cases.values())
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

    public int getSkipped()
    {
        if (skipped < 0)
        {
            int result = 0;

            for (PersistentTestSuiteResult child : suites)
            {
                result += child.getSkipped();
            }

            for (PersistentTestCaseResult child : cases.values())
            {
                result += child.getSkipped();
            }

            return result;
        }
        else
        {
            return skipped;
        }
    }

    public int getTotal()
    {
        if (total < 0)
        {
            int result = 0;

            for (PersistentTestSuiteResult child : suites)
            {
                result += child.getTotal();
            }

            for (PersistentTestCaseResult child : cases.values())
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

    public List<PersistentTestSuiteResult> getSuites()
    {
        return suites;
    }

    public Collection<PersistentTestCaseResult> getCases()
    {
        return cases.values();
    }

    public boolean isEquivalent(PersistentTestResult otherResult)
    {
        if(!(otherResult instanceof PersistentTestSuiteResult))
        {
            return false;
        }

        PersistentTestSuiteResult other = (PersistentTestSuiteResult) otherResult;
        if(!super.isEquivalent(other))
        {
            return false;
        }

        List<PersistentTestSuiteResult> otherSuites = other.getSuites();
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

        Collection<PersistentTestCaseResult> otherCases = other.getCases();
        if(cases.size() != otherCases.size())
        {
            return false;
        }

        Iterator<PersistentTestCaseResult> ourIt = cases.values().iterator();
        Iterator<PersistentTestCaseResult> otherIt = otherCases.iterator();
        while(ourIt.hasNext())
        {
            if(!ourIt.next().isEquivalent(otherIt.next()))
            {
                return false;
            }
        }

        return true;
    }

    public PersistentTestSuiteResult getSuite(String name)
    {
        for(PersistentTestSuiteResult suite: suites)
        {
            if(suite.getName().equals(name))
            {
                return suite;
            }
        }

        return null;
    }

    private PersistentTestCaseResult convertCase(TestCaseResult caseResult)
    {
        String name = caseResult.getName();
        if (conflictResolution.isUniqueNameGenerated() && hasCase(name))
        {
            int addition = 2;
            while (hasCase(makeCaseName(name, addition)))
            {
                addition++;
            }

            name = makeCaseName(name, addition);
        }

        return new PersistentTestCaseResult(name, caseResult.getDuration(), caseResult.getStatus(), caseResult.getMessage());
    }

    private String makeCaseName(String name, int addition)
    {
        if (conflictResolution == NameConflictResolution.APPEND)
        {
            return name + addition;
        }
        else
        {
            return Integer.toString(addition) + name;
        }
    }
}
