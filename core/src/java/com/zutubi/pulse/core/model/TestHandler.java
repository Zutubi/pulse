package com.zutubi.pulse.core.model;

import nu.xom.Element;

/**
 */
public interface TestHandler
{
    void startSuite(TestSuiteResult suiteResult);
    boolean endSuite();
    void handleCase(TestCaseResult caseResult, Element element);
}
