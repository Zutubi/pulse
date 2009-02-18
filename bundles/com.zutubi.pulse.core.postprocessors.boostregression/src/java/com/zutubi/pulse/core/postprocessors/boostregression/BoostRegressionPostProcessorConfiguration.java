package com.zutubi.pulse.core.postprocessors.boostregression;

import com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link BoostRegressionPostProcessor}.
 */
@SymbolicName("zutubi.boostRegressionPostProcessorConfig")
public class BoostRegressionPostProcessorConfiguration extends XMLTestReportPostProcessorConfigurationSupport
{
    public BoostRegressionPostProcessorConfiguration()
    {
        super(BoostRegressionPostProcessor.class, "Boost.Regression");
    }
}
