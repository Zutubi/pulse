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

package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A chain bootstrapper is a list of bootstrappers that are
 * run sequentially.
 */
public class ChainBootstrapper extends BootstrapperSupport
{
    private List<Bootstrapper> bootstrappers = new LinkedList<Bootstrapper>();
    private Bootstrapper currentBootstrapper;

    public ChainBootstrapper(Bootstrapper ...bootstrappers)
    {
        this.bootstrappers.addAll(Arrays.asList(bootstrappers));
    }

    public ChainBootstrapper add(Bootstrapper bootstrapper)
    {
        bootstrappers.add(bootstrapper);
        return this;
    }

    public void doBootstrap(CommandContext commandContext) throws BuildException
    {
        for (Bootstrapper bootstrapper : bootstrappers)
        {
            synchronized (this)
            {
                if (isTerminated())
                {
                    break;
                }
                currentBootstrapper = bootstrapper;
            }
            bootstrapper.bootstrap(commandContext);
        }
        currentBootstrapper = null;
    }

    public synchronized void terminate()
    {
        super.terminate();
        
        if(currentBootstrapper != null)
        {
            currentBootstrapper.terminate();
        }
    }
}
