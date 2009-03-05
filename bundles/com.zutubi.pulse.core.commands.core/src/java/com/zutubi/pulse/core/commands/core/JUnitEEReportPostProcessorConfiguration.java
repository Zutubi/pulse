package com.zutubi.pulse.core.commands.core;

import com.zutubi.tove.annotations.SymbolicName;

/**
 * A post-processor for JUnitEE XML reports - which are identical to JUnit
 * XML reports apart from some name tweaks.
 */
@SymbolicName("zutubi.juniteeReportPostProcessorConfig")
public class JUnitEEReportPostProcessorConfiguration extends JUnitReportPostProcessorConfiguration
{
    public JUnitEEReportPostProcessorConfiguration()
    {
        super(JUnitReportPostProcessor.class);
        setSuiteElement("juniteetestsuite");
        setCaseElement("juniteetestcase");
    }
}
