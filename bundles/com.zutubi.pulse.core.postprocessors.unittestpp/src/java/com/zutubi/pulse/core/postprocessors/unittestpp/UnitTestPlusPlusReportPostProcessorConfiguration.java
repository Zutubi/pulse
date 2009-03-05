package com.zutubi.pulse.core.postprocessors.unittestpp;

import com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link UnitTestPlusPlusReportPostProcessor}.
 */
@SymbolicName("zutubi.unitTestPlusPlusReportPostProcessorConfig")
public class UnitTestPlusPlusReportPostProcessorConfiguration extends XMLTestReportPostProcessorConfigurationSupport
{
    public UnitTestPlusPlusReportPostProcessorConfiguration()
    {
        super(UnitTestPlusPlusReportPostProcessor.class, "UnitTest++");
    }
}