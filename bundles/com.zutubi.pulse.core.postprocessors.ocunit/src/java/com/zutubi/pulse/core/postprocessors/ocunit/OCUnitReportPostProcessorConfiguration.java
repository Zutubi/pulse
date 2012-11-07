package com.zutubi.pulse.core.postprocessors.ocunit;

import com.zutubi.pulse.core.postprocessors.api.TestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link OCUnitReportPostProcessor}.
 */
@SymbolicName("zutubi.ocUnitReportPostProcessorConfig")
@Form(fieldOrder = {"name", "failOnFailure", "suite", "shortenSuiteNames", "resolveConflicts", "expectedFailureFile"})
public class OCUnitReportPostProcessorConfiguration extends TestReportPostProcessorConfigurationSupport
{
    private boolean shortenSuiteNames;

    public OCUnitReportPostProcessorConfiguration()
    {
        super(OCUnitReportPostProcessor.class);
    }

    public OCUnitReportPostProcessorConfiguration(String name)
    {
        super(OCUnitReportPostProcessor.class);
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