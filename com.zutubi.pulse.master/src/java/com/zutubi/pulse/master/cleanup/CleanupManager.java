package com.zutubi.pulse.master.cleanup;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.master.security.PulseThreadFactory;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The cleanup manager is reponsible for processing cleanup requests.
 * <p/>
 * Cleanup requests are queued and processed sequentially.
 */
public class CleanupManager implements Stoppable
{
    private static final Messages I18N = Messages.getInstance(CleanupManager.class);

    private PulseThreadFactory threadFactory;

    private LinkedBlockingQueue<CleanupRequest> queue = new LinkedBlockingQueue<CleanupRequest>();

    private Thread cleanupThread;
    private boolean running;

    public void init()
    {
        setRunning(true);
        cleanupThread = threadFactory.newThread(new Runnable()
        {
            public void run()
            {
                while (true)
                {
                    try
                    {
                        if (!isRunning())
                        {
                            return;
                        }

                        CleanupRequest request = queue.take();
                        request.process();
                    }
                    catch (InterruptedException e)
                    {
                        // interrupt expected when shutting down.
                    }
                }
            }
        }, I18N.format("service.name"));

        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    public void stop(boolean force)
    {
        if (cleanupThread != null)
        {
            setRunning(false);
            cleanupThread.interrupt();
        }
    }

    private synchronized boolean isRunning()
    {
        return running;
    }

    private synchronized void setRunning(boolean b)
    {
        this.running = b;
    }

    /**
     * Process the list of cleanup requests.
     *
     * @param requests  a list of cleanup requests.
     * @see #process(CleanupRequest)
     */
    public void process(List<CleanupRequest> requests)
    {
        for (CleanupRequest request : requests)
        {
            process(request);
        }
    }

    /**
     * Process the specified cleanup request at some time in the future.
     *
     * @param request   the request to process.
     *
     * @return true if the request has been accepted for processing, false
     * if a similar request already exists and hence this one was not accepted.
     */
    public boolean process(CleanupRequest request)
    {
        if (!queue.contains(request))
        {
            queue.add(request);
            return true;
        }
        return false;
    }

    public void setThreadFactory(PulseThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }
}
