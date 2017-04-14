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
 * Basic interface for commands.  Commands are single units of work in a
 * recipe.  The most common example is launching an external build tool
 * (like ant or make) to build supplied targets.
 */
public interface Command
{
    /**
     * Execute the command, providing feedback via the given context.
     *
     * @param commandContext context used to provide feedback of the command's
     *                       result and register captures
     */
    void execute(CommandContext commandContext);

    /**
     * The terminate method allows the command's execution to be interupted.
     * It may be called at any time after a call to execute, from any thread.
     */
    void terminate();
}
