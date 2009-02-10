package com.zutubi.pulse.core.postprocessors.cunit;

import com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link CUnitReportPostProcessor}.
 */
@SymbolicName("zutubi.cUnitReportPostProcessorConfig")
public class CUnitReportPostProcessorConfiguration extends XMLTestReportPostProcessorConfigurationSupport
{
    public CUnitReportPostProcessorConfiguration()
    {
        super(CUnitReportPostProcessor.class, "CUnit");
    }
}