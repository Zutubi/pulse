package com.cinnamonbob;

import com.cinnamonbob.core.BobException;
import com.cinnamonbob.core.Stoppable;
import com.cinnamonbob.events.Event;
import com.cinnamonbob.events.EventListener;
import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.events.build.RecipeCompletedEvent;
import com.cinnamonbob.events.build.RecipeDispatchedEvent;
import com.cinnamonbob.events.build.RecipeErrorEvent;
import com.cinnamonbob.events.build.RecipeEvent;
import com.cinnamonbob.util.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <class-comment/>
 */
public class ThreadedRecipeQueue implements Runnable, RecipeQueue, EventListener, Stoppable
{
    private static final Logger LOG = Logger.getLogger(ThreadedRecipeQueue.class);

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

    /**
     * Maps from recipe ID to the build service executing the recipe.
     */
    private final Map<Long, BuildService> executingServices = new TreeMap<Long, BuildService>();

    private ExecutorService executor;

    private boolean stopRequested = false;

    private boolean isRunning = false;

    private EventManager eventManager;

    public ThreadedRecipeQueue()
    {

    }

    public void init()
    {
        availableServices.add(new MasterBuildService());
        eventManager.register(this);
        start();
    }

    public void start()
    {
        if (isRunning())
        {
            throw new IllegalStateException("The queue is already running.");
        }
        LOG.debug("start();");
        executor = Executors.newSingleThreadExecutor();
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

    public List<RecipeDispatchRequest> takeSnapshot()
    {
        List<RecipeDispatchRequest> snapshot = new LinkedList<RecipeDispatchRequest>();

        lock.lock();
        try
        {
            snapshot.addAll(queuedDispatches);
            snapshot.addAll(newDispatches);
        }
        finally
        {
            lock.unlock();
        }

        return snapshot;
    }

    public boolean cancelRequest(long id)
    {
        boolean removed = false;

        try
        {
            lock.lock();
            RecipeDispatchRequest removeRequest = null;

            for (RecipeDispatchRequest request : newDispatches)
            {
                if (request.getRequest().getId() == id)
                {
                    removeRequest = request;
                    break;
                }
            }

            if (removeRequest != null)
            {
                newDispatches.remove(removeRequest);
                removed = true;
            }
            else
            {
                for (RecipeDispatchRequest request : queuedDispatches)
                {
                    if (request.getRequest().getId() == id)
                    {
                        removeRequest = request;
                        break;
                    }
                }

                if (removeRequest != null)
                {
                    queuedDispatches.remove(removeRequest);
                    removed = true;
                }
            }
        }
        finally
        {
            lock.unlock();
        }

        return removed;
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
        LOG.debug("started.");

        // wait for changes to either of the inbound queues. When change detected,
        // copy the new data into the internal queue (to minimize locked time) and
        // start processing.  JS: extended lock time to simplify snapshotting:
        // review iff this leads to a performance issue (seems unlikely).

        while (!stopRequested)
        {
            lock.lock();
            LOG.debug("lock.lock();");
            try
            {
                if (newDispatches.size() == 0 && newServices.size() == 0)
                {
                    try
                    {
                        LOG.debug("lockCondition.await();");
                        lockCondition.await(60, TimeUnit.SECONDS);
                        LOG.debug("lockCondition.unawait();");
                    }
                    catch (InterruptedException e)
                    {
                        LOG.debug("lockCondition.wait() was interrupted: " + e.getMessage());
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

                List<RecipeDispatchRequest> dispatchedRequests = new LinkedList<RecipeDispatchRequest>();
                List<BuildService> unavailableServices = new LinkedList<BuildService>();

                for (RecipeDispatchRequest request : queuedDispatches)
                {
                    for (BuildService service : availableServices)
                    {
                        // can the request be sent to this service?
                        if (request.getHostRequirements().fulfilledBy(service) && !unavailableServices.contains(service))
                        {
                            dispatchRequest(request, service, unavailableServices, dispatchedRequests);
                            break;
                        }
                    }
                }

                queuedDispatches.removeAll(dispatchedRequests);
                availableServices.removeAll(unavailableServices);
            }
            finally
            {
                lock.unlock();
                LOG.debug("lock.unlock();");
            }
        }

        executor.shutdown();
        LOG.debug("stopped.");
        isRunning = false;
    }

    private void dispatchRequest(RecipeDispatchRequest request, BuildService service, List<BuildService> unavailableServices, List<RecipeDispatchRequest> dispatchedRequests)
    {
        dispatchedRequests.add(request);

        try
        {
            request.prepare();
        }
        catch (Exception e)
        {
            eventManager.publish(new RecipeErrorEvent(this, request.getRequest().getId(), "Error dispatching recipe: " + e.getMessage()));
            return;
        }

        service.build(request.getRequest());
        unavailableServices.add(service);
        lock.lock();
        try
        {
            executingServices.put(request.getRequest().getId(), service);
        }
        finally
        {
            lock.unlock();
        }
        eventManager.publish(new RecipeDispatchedEvent(this, request.getRequest().getId(), service));
    }

    public void stop(boolean force)
    {
        if (isStopped())
        {
            throw new IllegalStateException("The queue is already stopped.");
        }

        lock.lock();
        try
        {
            LOG.debug("stop();");
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

    public void handleEvent(Event evt)
    {
        RecipeEvent event = (RecipeEvent) evt;

        lock.lock();
        try
        {
            BuildService service = executingServices.get(event.getRecipeId());

            // The service could be null if there was a temporary loss of
            // communication with the build service leading to abortion of the
            // recipe on the master.
            if (service != null)
            {
                executingServices.remove(event.getRecipeId());
                available(service);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{RecipeCompletedEvent.class, RecipeErrorEvent.class};
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

}
