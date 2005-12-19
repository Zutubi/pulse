package com.cinnamonbob;

import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.events.build.RecipeDispatchedEvent;
import com.cinnamonbob.core.event.EventManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <class-comment/>
 */
public class DefaultRecipeQueue implements Runnable
{
    private static final Logger LOG = Logger.getLogger(DefaultRecipeQueue.class.getName());

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition lockCondition = lock.newCondition();

    /**
     * The queue to which new dispatch requests are added.
     */
    private final List<RecipeDispatchRequest> newDispatches = new LinkedList<RecipeDispatchRequest>();

    /**
     * The internal queue of dispatch requests.
     */
    private final List<RecipeDispatchRequest> queuedDispatches = new LinkedList<RecipeDispatchRequest>();

    private final List<BuildService> newServices = new LinkedList<BuildService>();
    private final List<BuildService> availableServices = new LinkedList<BuildService>();

    private final ExecutorService executor;

    private boolean stopRequested = false;

    private boolean isRunning = false;

    private EventManager eventManager;

    public DefaultRecipeQueue()
    {
        executor = Executors.newSingleThreadExecutor();
    }

    public void start()
    {
        if (isRunning())
        {
            throw new IllegalStateException("The queue is already running.");
        }
        LOG.info("start();");
        executor.execute(this);
    }

    /**
     * Enqueue a new recipe dispatch request.
     *
     * @param dispatchRequest
     */
    public void enqueue(RecipeDispatchRequest dispatchRequest)
    {
        lock.lock();
        try
        {
            newDispatches.add(dispatchRequest);
            lockCondition.signal();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void available(BuildService buildService)
    {
        lock.lock();
        try
        {
            newServices.add(buildService);
            lockCondition.signal();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void run()
    {
        isRunning = true;
        stopRequested = false;
        LOG.info("started.");

        // wait for changes to either of the inbound queues. When change detected,
        // copy the new data into the internal queue (to minimize locked time) and
        // start processing.

        while (!stopRequested)
        {
            lock.lock();
            LOG.info("lock.lock();");
            try
            {
                if (newDispatches.size() == 0 && newServices.size() == 0)
                {
                    try
                    {
                        LOG.info("lockCondition.await();");
                        lockCondition.await(60, TimeUnit.SECONDS);
                        LOG.info("lockCondition.unawait();");
                    }
                    catch (InterruptedException e)
                    {
                        LOG.info("lockCondition.wait() was interrupted: " + e.getMessage());
                    }
                }

                if (stopRequested)
                {
                    break;
                }

                queuedDispatches.addAll(newDispatches);
                newDispatches.clear();
                availableServices.addAll(newServices);
                newServices.clear();
            }
            finally
            {
                lock.unlock();
                LOG.info("lock.unlock();");
            }

            List<RecipeDispatchRequest> dispatchedRequests = new LinkedList<RecipeDispatchRequest>();
            List<BuildService> unavailableServices = new LinkedList<BuildService>();

            for (RecipeDispatchRequest request : queuedDispatches)
            {
                for (BuildService service : availableServices)
                {
                    // can the request be sent to this service?
                    if (request.getHostRequirements().fulfilledBy(service))
                    {
                        service.build(request.getRequest());
                        unavailableServices.add(service);
                        dispatchedRequests.add(request);
                        eventManager.publish(new RecipeDispatchedEvent(this, request.getRequest().getId(), service));
                    }
                }
            }

            queuedDispatches.removeAll(dispatchedRequests);
            availableServices.removeAll(unavailableServices);
        }
        LOG.info("stopped.");
        isRunning = false;
    }

    public void stop()
    {
        if (isStopped())
        {
            throw new IllegalStateException("The queue is already stopped.");
        }

        lock.lock();
        try
        {
            LOG.info("stop();");
            stopRequested = true;
            lockCondition.signal();
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean isStopped()
    {
        return !isRunning();
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    public int length()
    {
        lock.lock();
        try
        {
            return queuedDispatches.size() + newDispatches.size();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
