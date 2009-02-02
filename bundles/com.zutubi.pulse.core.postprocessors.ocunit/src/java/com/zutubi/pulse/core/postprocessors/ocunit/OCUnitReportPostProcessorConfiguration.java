package com.zutubi.pulse.core.postprocessors.ocunit;

import com.zutubi.pulse.core.postprocessors.api.TestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.ocUnitReportPostProcessorConfig")
public class OCUnitReportPostProcessorConfiguration extends TestReportPostProcessorConfigurationSupport
{
    public OCUnitReportPostProcessorConfiguration()
    {
        super(OCUnitReportPostProcessor.class);
    }
}