package com.zutubi.pulse.core.postprocessors.qtestlib;

import com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link QTestLibReportPostProcessor}.
 */
@SymbolicName("zutubi.qtestLibReportPostProcessorConfig")
public class QTestLibReportPostProcessorConfiguration extends XMLTestReportPostProcessorConfigurationSupport
{
    public QTestLibReportPostProcessorConfiguration()
    {
        super(QTestLibReportPostProcessor.class, "QTestLib");
    }
}