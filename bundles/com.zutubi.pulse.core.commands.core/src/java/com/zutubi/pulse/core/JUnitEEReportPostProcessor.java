package com.zutubi.pulse.core;

/**
 * A post-processor for JUnitEE XML reports - which are identical to JUnit
 * XML reports apart from some name tweaks.
 */
public class JUnitEEReportPostProcessor extends JUnitReportPostProcessor
{
    public JUnitEEReportPostProcessor()
    {
        super("JUnitEE");
        setSuiteElement("juniteetestsuite");
        setCaseElement("juniteetestcase");
    }
}
