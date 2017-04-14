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

package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for a test command that captures the recipe status in the
 * command context. 
 */
@SymbolicName("zutubi.captureStatusCommandConfig")
public class CaptureStatusCommandConfiguration extends CommandConfigurationSupport
{
    private boolean fail = false;
    
    public CaptureStatusCommandConfiguration()
    {
        super(CaptureStatusCommand.class);
    }

    public boolean isFail()
    {
        return fail;
    }

    public void setFail(boolean fail)
    {
        this.fail = fail;
    }
}