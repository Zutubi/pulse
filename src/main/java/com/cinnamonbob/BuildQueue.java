package com.cinnamonbob;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The BuildQueue is the centralised queue from which BuildRequest objects
 * are dispatched for processing.
 * <p/>
 * <p>The BuildQueue instantiates a Thread to handle the dispatching of build
 * requests. When a BuildQueue is no longer needed, invoke {@link #stop()} to
 * stop the internal Thread.
 * <p>The build queue supports multiple dispatch algorithms via the implementation
 * of the BuildDispatcher interface.
 *
 * @author Daniel Ostermeier
 */
public class BuildQueue implements Runnable, Iterable<BuildRequest>
{

    private final LinkedBlockingQueue<BuildRequest> queue = new LinkedBlockingQueue<BuildRequest>();

    private final Thread t;

    private int requestsDispatched = 0;

    private BuildDispatcher dispatcher = null;

    public BuildQueue()
    {
        t = new Thread(this);
        t.start();
    }

    /**
     * Stop the build queue. Once stopped, a build queue can not be restarted.
     */
    public void stop()
    {
        t.interrupt();
    }

    /**
     * Enqueue a build request for processing
     *
     * @param r
     */
    public void enqueue(BuildRequest r)
    {
        try
        {
            queue.put(r);
        } catch (InterruptedException e)
        {
        }
    }

    public void setDispatcher(BuildDispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
    }

    /**
     * Implementation of {@link Runnable#run()} method.
     */
    public void run()
    {
        try
        {
            while (true)
            {
                if (Thread.currentThread().isInterrupted())
                {
                    break;
                }
                dispatch((BuildRequest) queue.take());
            }
        } catch (InterruptedException e)
        {
        }

    }

    /**
     * Dispatch the build request to where it can be processed.
     *
     * @param r
     */
    private void dispatch(BuildRequest r)
    {
        try
        {
            dispatcher.dispatch(r);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Return the count of the number of build requests that have been dispatched
     * from this build queue.
     *
     * @return
     */
    public int getDispatchedCount()
    {
        return requestsDispatched;
    }

    
    public Iterator<BuildRequest> iterator()
    {
        return queue.iterator();
    }
}
