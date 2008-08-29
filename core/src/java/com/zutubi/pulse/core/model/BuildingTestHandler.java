package com.zutubi.pulse.core.model;

import nu.xom.Element;

import java.util.Stack;

/**
 */
public class BuildingTestHandler implements TestHandler
{
    private TestSuiteResult top;
    private Stack<TestSuiteResult> suites = new Stack<TestSuiteResult>();

    public TestSuiteResult getTop()
    {
        return top;
    }

    public void startSuite(TestSuiteResult suiteResult)
    {
        if(suites.size() > 0)
        {
            suites.peek().add(suiteResult, TestSuiteResult.Resolution.OFF);
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

    public void handleCase(TestCaseResult caseResult, Element element)
    {
        suites.peek().add(caseResult, TestSuiteResult.Resolution.OFF);
    }
}
