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

package com.zutubi.pulse.dev.local;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;

/**
 * Bootstrapper for local builds.
 */
public class LocalBootstrapper extends BootstrapperSupport
{
    public void doBootstrap(CommandContext commandContext) throws BuildException
    {
        // Noop.  Ideally the processor would not require a bootstrapper, but
        // that comes with extra baggage not worth pursuing at this moment.
    }
}
