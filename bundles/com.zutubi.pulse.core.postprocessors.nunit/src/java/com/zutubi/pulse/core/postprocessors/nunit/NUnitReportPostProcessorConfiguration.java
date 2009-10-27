package com.zutubi.pulse.core.postprocessors.nunit;

import com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link NUnitReportPostProcessor}.
 */
@SymbolicName("zutubi.nUnitReportPostProcessorConfig")
public class NUnitReportPostProcessorConfiguration extends XMLTestReportPostProcessorConfigurationSupport
{
    public NUnitReportPostProcessorConfiguration()
    {
        super(NUnitReportPostProcessor.class, "NUnit");
    }
}