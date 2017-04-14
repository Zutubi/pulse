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

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.CommandSupport;

import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_RECIPE_STATUS;

/**
 * Simple testing command captures the recipe status from the execution context
 * to the properties stored on the command result.
 */
public class CaptureStatusCommand extends CommandSupport
{
    public static final String FAILURE_MESSAGE = "configured to fail";

    public CaptureStatusCommand(CaptureStatusCommandConfiguration config)
    {
        super(config);
    }

    public void execute(CommandContext commandContext)
    {
        commandContext.addCommandProperty(PROPERTY_RECIPE_STATUS, commandContext.getExecutionContext().getString(PROPERTY_RECIPE_STATUS));
        
        if (((CaptureStatusCommandConfiguration) getConfig()).isFail())
        {
            commandContext.failure(FAILURE_MESSAGE);
        }
    }
}
