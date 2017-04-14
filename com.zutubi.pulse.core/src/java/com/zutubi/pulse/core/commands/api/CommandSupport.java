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

package com.zutubi.pulse.core.commands.api;

/**
 * Support class to make implementing the Command interface simpler for the
 * simple cases.
 */
public abstract class CommandSupport implements Command
{
    private CommandConfigurationSupport config;

    /**
     * Constructor that stores the configuration for later access via
     * {@link #getConfig()}.
     *
     * @param config the configuration for this command
     */
    protected CommandSupport(CommandConfigurationSupport config)
    {
        this.config = config;
    }

    /**
     * Returns the configuration for this command.
     *
     * @return this command's configuration
     */
    public CommandConfigurationSupport getConfig()
    {
        return config;
    }

    public void terminate()
    {
    }
}
