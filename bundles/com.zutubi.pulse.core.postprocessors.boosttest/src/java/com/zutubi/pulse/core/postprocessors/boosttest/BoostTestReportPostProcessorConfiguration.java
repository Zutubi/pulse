/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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