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

package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;

/**
 * Helper base class for configuration of processors that find features in
 * output.
 */
@SymbolicName("zutubi.outputPostProcessorConfigSupport")
public abstract class OutputPostProcessorConfigurationSupport extends PostProcessorConfigurationSupport
{
    @Wizard.Ignore
    private boolean failOnError = true;
    @Wizard.Ignore
    private boolean failOnWarning = false;

    protected OutputPostProcessorConfigurationSupport(Class<? extends PostProcessor> postProcessorType)
    {
        super(postProcessorType);
    }

    /**
     * @see #setFailOnError(boolean)
     * @return current value of the fail on error flag
     */
    public boolean isFailOnError()
    {
        return failOnError;
    }

    /**
     * If set to true, the command (and thus build) will be failed when this
     * processor detects an error feature.  This flag is true by default.
     *
     * @param failOnError true to fail the build on error
     */
    public void setFailOnError(boolean failOnError)
    {
        this.failOnError = failOnError;
    }

    /**
     * @see #setFailOnWarning(boolean)
     * @return current value of the fail on warning flag
     */
    public boolean isFailOnWarning()
    {
        return failOnWarning;
    }

    /**
     * If set to true, the command (and thus build) will be failed when this
     * processor detects a warning feature.  This flag is false by default.
     *
     * @param failOnWarning true to fail the build on error
     */
    public void setFailOnWarning(boolean failOnWarning)
    {
        this.failOnWarning = failOnWarning;
    }
}
