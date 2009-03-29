package com.zutubi.pulse.core.postprocessors.boosttest;

import com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link BoostTestReportPostProcessor}.
 */
@SymbolicName("zutubi.boostTestPostProcessorConfig")
public class BoostTestReportPostProcessorConfiguration extends XMLTestReportPostProcessorConfigurationSupport
{
    public BoostTestReportPostProcessorConfiguration()
    {
        super(BoostTestReportPostProcessor.class, "Boost.Test");
    }
}