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

package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.CommandContext;

/**
 */
public class NamedArgumentCommand extends ExecutableCommand
{
    public NamedArgumentCommand(NamedArgumentCommandConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    public NamedArgumentCommandConfiguration getConfig()
    {
        return (NamedArgumentCommandConfiguration) super.getConfig();
    }

    @Override
    public void execute(CommandContext commandContext)
    {
        for (NamedArgumentCommandConfiguration.NamedArgument arg: getConfig().getNamedArguments())
        {
            commandContext.addCommandProperty(arg.getName(), arg.getValue());
        }
        
        super.execute(commandContext);
    }
}
