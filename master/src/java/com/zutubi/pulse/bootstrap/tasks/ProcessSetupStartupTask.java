package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.bootstrap.StartupTask;
import com.zutubi.pulse.spring.SpringComponentContext;
import com.zutubi.pulse.bootstrap.WebManager;
import com.zutubi.pulse.bootstrap.SetupManager;

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

    public ProcessSetupStartupTask()
    {
        setupContexts = new LinkedList<String>();
        setupContexts.add("com/zutubi/pulse/bootstrap/context/setupContext.xml");
    }

    public void execute()
    {
        // load the setup context. we do not expect this to take long, so we dont worry about a holding page here.
        SpringComponentContext.addClassPathContextDefinitions(setupContexts.toArray(new String[setupContexts.size()]));

        // Deploy the setup xwork configuration.
        WebManager webManager = (WebManager) SpringComponentContext.getBean("webManager");
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

        // handle the initialisation of the security manager, since this can not be done within the spring context file.
//        AcegiSecurityManager securityManager = (AcegiSecurityManager) ComponentContext.getBean("securityManager");
//        securityManager.secure();
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void run()
    {
        SetupManager setupManager = (SetupManager) SpringComponentContext.getBean("setupManager");
        setupManager.startSetupWorkflow(this);
    }

    public void finaliseSetup()
    {
        setupCompleteFlag.release();
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }
}
