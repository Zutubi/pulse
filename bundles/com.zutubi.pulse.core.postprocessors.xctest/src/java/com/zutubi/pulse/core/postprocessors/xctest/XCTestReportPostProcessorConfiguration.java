package com.zutubi.pulse.core.postprocessors.xctest;

import com.zutubi.pulse.core.postprocessors.api.TestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link XCTestReportPostProcessor}.
 */
@SymbolicName("zutubi.xcTestReportPostProcessorConfig")
@Form(fieldOrder = {"name", "failOnFailure", "suite", "shortenSuiteNames", "resolveConflicts", "expectedFailureFile"})
public class XCTestReportPostProcessorConfiguration extends TestReportPostProcessorConfigurationSupport
{
    private boolean shortenSuiteNames;

    public XCTestReportPostProcessorConfiguration()
    {
        super(XCTestReportPostProcessor.class);
    }

    public XCTestReportPostProcessorConfiguration(String name)
    {
        super(XCTestReportPostProcessor.class);
        setName(name);
    }

    /**
     * @return true if suite names that look like paths should be shortened where possible
     */
    public boolean isShortenSuiteNames()
    {
        return shortenSuiteNames;
    }

    public void setShortenSuiteNames(boolean shortenSuiteNames)
    {
        this.shortenSuiteNames = shortenSuiteNames;
    }
}
