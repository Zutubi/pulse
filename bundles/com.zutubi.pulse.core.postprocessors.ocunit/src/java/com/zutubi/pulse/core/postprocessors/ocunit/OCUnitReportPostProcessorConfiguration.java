package com.zutubi.pulse.core.postprocessors.ocunit;

import com.zutubi.pulse.core.postprocessors.api.TestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link OCUnitReportPostProcessor}.
 */
@SymbolicName("zutubi.ocUnitReportPostProcessorConfig")
public class OCUnitReportPostProcessorConfiguration extends TestReportPostProcessorConfigurationSupport
{
    public OCUnitReportPostProcessorConfiguration()
    {
        super(OCUnitReportPostProcessor.class);
    }

    public OCUnitReportPostProcessorConfiguration(String name)
    {
        super(OCUnitReportPostProcessor.class);
        setName(name);
    }
}