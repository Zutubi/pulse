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
import com.zutubi.pulse.core.engine.api.BuildException;

/**
 * This interface defines processes that are run to bootstrap a build.  They are
 * responsible for initialising so that it is ready for a build.
 *
 * The bootstrap process is integrated into a build into the form of the first command
 * that is executed, and as such works with a empty directory.
 *
 * Details are made available to the bootstrap process via the {@link CommandContext}.
 */
public interface Bootstrapper
{
    /**
     * Handle the bootstrapping.
     *
     * @param commandContext    the context details in which the bootstrap is being run.
     *
     * @throws BuildException if the bootstrapping is unable to prepare the builds working
     * directory. 
     */
    void bootstrap(CommandContext commandContext) throws BuildException;

    /**
     * This method is triggered if bootstrapping should be stopped.  This method
     * should not block waiting for the bootstrapper to finish. 
     */
    void terminate();
}
