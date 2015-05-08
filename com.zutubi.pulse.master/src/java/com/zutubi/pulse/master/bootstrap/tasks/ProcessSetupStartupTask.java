package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.bootstrap.SetupManager;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.servercore.bootstrap.StartupException;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

import java.util.LinkedList;
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
        setupContexts = new LinkedList<String>();
        setupContexts.add("com/zutubi/pulse/master/bootstrap/context/setupContext.xml");
    }

    public void execute()
    {
        // load the setup context. we do not expect this to take long, so we don't
        // worry about a holding page here.
        SpringComponentContext.addClassPathContextDefinitions(setupContexts.toArray(new String[setupContexts.size()]));

        // Deploy the setup xwork configuration.
        WebManager webManager = SpringComponentContext.getBean("webManager");
        webManager.deploySetup();

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
        SetupManager setupManager = SpringComponentContext.getBean("setupManager");
        setupManager.startSetupWorkflow(this);
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
