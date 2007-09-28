package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.security.AcegiSecurityManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;

/**
 */
public class ProcessSetupStartupTask implements Runnable, StartupTask
{
    private List<String> coreContexts;
    private List<String> setupContexts;
    private Semaphore setupCompleteFlag = new Semaphore(0);
    private ThreadFactory threadFactory;

    public ProcessSetupStartupTask()
    {
        coreContexts = new LinkedList<String>();
        coreContexts.add("com/zutubi/pulse/bootstrap/coreSubsystemContext.xml");
        setupContexts = new LinkedList<String>();
        setupContexts.add("com/zutubi/pulse/bootstrap/configSubsystemContext.xml");
    }

    public void execute()
    {
        // load the core context, common to all of the system configurations.
        ComponentContext.addClassPathContextDefinitions(coreContexts.toArray(new String[coreContexts.size()]));

        // load the setup context. we do not expect this to take long, so we dont worry about a holding page here.
        ComponentContext.addClassPathContextDefinitions(setupContexts.toArray(new String[setupContexts.size()]));

        // startup the web server.
        WebManager webManager = (WebManager) ComponentContext.getBean("webManager");

        // i) set the system starting pages (periodically refresh)
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
        AcegiSecurityManager securityManager = (AcegiSecurityManager) ComponentContext.getBean("securityManager");
        securityManager.init();
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void run()
    {
        SetupManager setupManager = (SetupManager) ComponentContext.getBean("setupManager");
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
