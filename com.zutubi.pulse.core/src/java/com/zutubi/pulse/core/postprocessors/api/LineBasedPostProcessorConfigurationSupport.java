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
import com.zutubi.validation.annotations.Min;

/**
 * A support base class for configuration of {@link LineBasedPostProcessorSupport}
 * instances.
 */
@SymbolicName("zutubi.lineBasedPostProcessorConfigSupport")
public abstract class LineBasedPostProcessorConfigurationSupport extends TextFilePostProcessorConfigurationSupport
{
    @Min(0)
    private int leadingContext = 0;
    @Min(0)
    private int trailingContext = 0;
    @Wizard.Ignore
    private boolean joinOverlapping = true;

    protected LineBasedPostProcessorConfigurationSupport(Class<? extends LineBasedPostProcessorSupport> postProcessorType)
    {
        super(postProcessorType);
    }

    /**
     * @see #setLeadingContext(int)
     * @return the number of lines of leading context to capture
     */
    public int getLeadingContext()
    {
        return leadingContext;
    }

    /**
     * Sets the number of lines of leading context to capture with any
     * discovered feature (zero by default).
     *
     * @param leadingContext the number of lines to capture (may be zero)
     */
    public void setLeadingContext(int leadingContext)
    {
        this.leadingContext = leadingContext;
    }

    /**
     * @see #setTrailingContext(int)
     * @return the number of lines of trailing context to capture
     */
    public int getTrailingContext()
    {
        return trailingContext;
    }

    /**
     * Sets the number of lines of trailing context to capture with any
     * discovered feature (zero by default).
     *
     * @param trailingContext the number of lines to capture (may be zero)
     */
    public void setTrailingContext(int trailingContext)
    {
        this.trailingContext = trailingContext;
    }

    /**
     * @see #setJoinOverlapping(boolean)
     * @return true if overlapping features will be joined
     */
    public boolean isJoinOverlapping()
    {
        return joinOverlapping;
    }

    /**
     * If set to true, overlapping features (as determined by the first and
     * last lines captured - context included) will be joined into a single
     * feature.
     *
     * @param joinOverlapping true to join overlapping features
     */
    public void setJoinOverlapping(boolean joinOverlapping)
    {
        this.joinOverlapping = joinOverlapping;
    }
}