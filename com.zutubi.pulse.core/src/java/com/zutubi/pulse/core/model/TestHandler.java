package com.zutubi.pulse.core.model;

import nu.xom.Element;

/**
 */
public interface TestHandler
{
    void startSuite(PersistentTestSuiteResult suiteResult);
    boolean endSuite();
    void handleCase(PersistentTestCaseResult caseResult, Element element);
}
