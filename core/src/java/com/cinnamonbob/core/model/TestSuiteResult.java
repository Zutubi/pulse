package com.zutubi.pulse.core.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents the results of a test suite: a group of child test suites and
 * cases unified under one name.
 */
public class TestSuiteResult extends TestResult
{
    /**
     * Results grouped within this suite.
     */
    private List<TestResult> children;

    public TestSuiteResult()
    {

    }

    public TestSuiteResult(String name)
    {
        this(name, UNKNOWN_DURATION);
    }

    public TestSuiteResult(String name, long duration)
    {
        super(name, duration);
        children = new LinkedList<TestResult>();
    }

    public List<TestResult> getChildren()
    {
        return children;
    }

    public void setChildren(List<TestResult> children)
    {
        this.children = children;
    }

    public void add(TestResult child)
    {
        children.add(child);
    }

    public int getErrors()
    {
        int result = 0;

        for (TestResult child : children)
        {
            result += child.getErrors();
        }

        return result;
    }

    public int getFailures()
    {
        int result = 0;

        for (TestResult child : children)
        {
            result += child.getFailures();
        }

        return result;
    }

    public int getTotal()
    {
        int result = 0;

        for (TestResult child : children)
        {
            result += child.getTotal();
        }

        return result;
    }

    public boolean isSuite()
    {
        return true;
    }
}
