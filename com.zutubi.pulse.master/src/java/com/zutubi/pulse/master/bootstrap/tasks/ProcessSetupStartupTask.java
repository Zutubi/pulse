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

package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.bootstrap.SetupManager;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.servercore.bootstrap.StartupException;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;

/**
 */
public class ProcessSetupStartupTask implements Runnable, StartupTask
{
    private List<String> setupContexts;
    private Semaphore setupCompleteFlag = new Semaphore(0);
    private ThreadFactory threadFactory;
    private Exception error;

    public ProcessSetupStartupTask()
    {
        setupContexts = new ArrayList<>();
        setupContexts.add("com/zutubi/pulse/master/bootstrap/context/setupContext.xml");
    }

    public void execute()
    {
        // load the setup context. we do not expect this to take long, so we don't
        // worry about a holding page here.
        SpringComponentContext.addClassPathContextDefinitions(setupContexts.toArray(new String[setupContexts.size()]));

        // This init doesn enough to allow us to start a web UI and start the setup app (if required) without taking
        // too long.
        SetupManager setupManager = SpringComponentContext.getBean("setupManager");
        setupManager.init(this);

        Thread setupThread = threadFactory.newThread(this);
        setupThread.start();
        try
        {
            setupCompleteFlag.acquire();
        }
        catch (InterruptedException e)
        {
            // Ignore
        }

        if (error != null)
        {
            throw new StartupException(error);
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void run()
    {
        // Deploy the setup xwork configuration first so a web UI becomes available.  This allows the user to see
        // the server status in their browser while it starts up (which can take a little while).
        WebManager webManager = SpringComponentContext.getBean("webManager");
        webManager.deploySetup();

        // This workflow can take a while if
        SetupManager setupManager = SpringComponentContext.getBean("setupManager");
        setupManager.startSetupWorkflow();
    }

    public void finaliseSetup(Exception error)
    {
        this.error = error;
        setupCompleteFlag.release();
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }
}
