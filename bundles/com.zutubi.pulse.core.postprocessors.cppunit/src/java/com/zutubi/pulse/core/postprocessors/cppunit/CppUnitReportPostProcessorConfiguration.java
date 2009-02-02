package com.zutubi.pulse.core.postprocessors.cppunit;

import com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.cppUnitReportPostProcessorConfig")
public class CppUnitReportPostProcessorConfiguration extends XMLTestReportPostProcessorConfigurationSupport
{
    public CppUnitReportPostProcessorConfiguration()
    {
        super(CppUnitReportPostProcessor.class, "CppUnit");
    }
}