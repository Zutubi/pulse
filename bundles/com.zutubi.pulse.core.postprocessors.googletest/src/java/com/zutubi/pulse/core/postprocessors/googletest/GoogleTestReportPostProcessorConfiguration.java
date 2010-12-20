package com.zutubi.pulse.core.postprocessors.googletest;

import com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorConfigurationSupport;
import com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link GoogleTestReportPostProcessor}.
 */
@SymbolicName("zutubi.googleTestReportPostProcessorConfig")
public class GoogleTestReportPostProcessorConfiguration extends JUnitReportPostProcessorConfiguration
{
    public GoogleTestReportPostProcessorConfiguration()
    {
        super(GoogleTestReportPostProcessor.class, "Google Test");
    }
}