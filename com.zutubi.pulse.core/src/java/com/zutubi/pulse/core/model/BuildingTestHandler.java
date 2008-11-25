package com.zutubi.pulse.core.model;

import nu.xom.Element;

import java.util.Stack;

/**
 */
public class BuildingTestHandler implements TestHandler
{
    private PersistentTestSuiteResult top;
    private Stack<PersistentTestSuiteResult> suites = new Stack<PersistentTestSuiteResult>();

    public PersistentTestSuiteResult getTop()
    {
        return top;
    }

    public void startSuite(PersistentTestSuiteResult suiteResult)
    {
        if(suites.size() > 0)
        {
            suites.peek().add(suiteResult);
        }
        else
        {
            top = suiteResult;
        }

        suites.push(suiteResult);
    }

    public boolean endSuite()
    {
        suites.pop();
        return false;
    }

    public void handleCase(PersistentTestCaseResult caseResult, Element element)
    {
        suites.peek().add(caseResult);
    }
}
