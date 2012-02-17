package com.zutubi.pulse.core.postprocessors.mstest;

import com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link MSTestReportPostProcessor}.
 */
@SymbolicName("zutubi.msTestReportPostProcessorConfig")
public class MSTestReportPostProcessorConfiguration extends XMLTestReportPostProcessorConfigurationSupport
{
    public MSTestReportPostProcessorConfiguration()
    {
        super(MSTestReportPostProcessor.class, "MSTest");
    }
}
