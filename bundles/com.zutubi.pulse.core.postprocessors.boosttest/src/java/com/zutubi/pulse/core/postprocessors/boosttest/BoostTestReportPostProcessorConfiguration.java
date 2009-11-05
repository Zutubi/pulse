package com.zutubi.pulse.core.postprocessors.boosttest;

import com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link BoostTestReportPostProcessor}.
 */
@SymbolicName("zutubi.boostTestPostProcessorConfig")
@Form(fieldOrder = {"name", "failOnFailure", "processMessages", "processInfo", "suite", "resolveConflicts", "expectedFailureFile"})
public class BoostTestReportPostProcessorConfiguration extends XMLTestReportPostProcessorConfigurationSupport
{
    private boolean processMessages = false;
    private boolean processInfo = false;

    public BoostTestReportPostProcessorConfiguration()
    {
        super(BoostTestReportPostProcessor.class, "Boost.Test");
    }

    public boolean isProcessMessages()
    {
        return processMessages;
    }

    public void setProcessMessages(boolean processMessages)
    {
        this.processMessages = processMessages;
    }

    public boolean isProcessInfo()
    {
        return processInfo;
    }

    public void setProcessInfo(boolean processInfo)
    {
        this.processInfo = processInfo;
    }
}