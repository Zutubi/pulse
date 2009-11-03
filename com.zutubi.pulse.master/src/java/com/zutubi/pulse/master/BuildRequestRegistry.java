package com.zutubi.pulse.master;

import com.zutubi.pulse.master.events.build.AbstractBuildRequestEvent;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.logging.Logger;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A registry of all build requests that have been published recently.  This
 * allows the status of those requests to be tracked while it changes
 * asynchronously.
 */
public class BuildRequestRegistry
{
    private static final Logger LOG = Logger.getLogger(BuildRequestRegistry.class);

    /**
     * Value returned for unknown build and assimilating request ids.
     */
    public static final int NONE = 0;

    private static final int DEFAULT_LIMIT = 10000;
    private static final int DEFAULT_TRIM  = 1000;

    private int limit = DEFAULT_LIMIT;
    private int trim = DEFAULT_TRIM;

    private AccessManager accessManager;

    /**
     * States in a request's lifecycle.
     */
    public enum RequestStatus
    {
        /**
         * The request is unknown to the registry.
         */
        UNKNOWN,
        /**
         * The request is known, but has not yet reached the queue.
         */
        UNHANDLED,
        /**
         * The request was rejected, possibly with a reason.
         */
        REJECTED,
        /**
         * The request was assimilated into an existing queued request.
         */
        ASSIMILATED,
        /**
         * The request has reached the queue and is awaiting activation.
         */
        QUEUED,
        /**
         * The request was explicitly cancelled by a user.
         */
        CANCELLED,
        /**
         * The request has been activated and now has a corresponding build number for further
         * tracking.
         */
        ACTIVATED
    }

    private ReentrantLock lock = new ReentrantLock();
    private Condition lockCondition = lock.newCondition();
    private Map<Long, RegEntry> requestIdToEntry = new HashMap<Long, RegEntry>();

    /**
     * Registers a new request with the registry.  Should be done before a
     * request event is published.
     *
     * @param event the new event to register
     */
    public void register(AbstractBuildRequestEvent event)
    {
        lock.lock();
        try
        {
            prune();
            requestIdToEntry.put(event.getId(), new RegEntry(event));
        }
        finally
        {
            lock.unlock();
        }
    }

    private void prune()
    {
        if (requestIdToEntry.size() >= limit)
        {
            List<Long> keys = new LinkedList<Long>(requestIdToEntry.keySet());
            Collections.sort(keys);
            keys = keys.subList(0, trim);
            for (Long key: keys)
            {
                requestIdToEntry.remove(key);
            }
        }
    }

    /**
     * Marks the request as rejected with the given reason.
     *
     * @param event  request to mark
     * @param reason human-readable reason for the rejection
     */
    void requestRejected(AbstractBuildRequestEvent event, final String reason)
    {
        transition(event, "Rejected", new UnaryProcedure<RegEntry>()
        {
            public void process(RegEntry regEntry)
            {
                regEntry.reject(reason);
            }
        });
    }

    /**
     * Marks the request as assimilated into an existing request.
     *
     * @param event         request to mark
     * @param intoRequestId id of the request that it was assimilated into
     */
    void requestAssimilated(AbstractBuildRequestEvent event, final long intoRequestId)
    {
        transition(event, "Assimilated", new UnaryProcedure<RegEntry>()
        {
            public void process(RegEntry regEntry)
            {
                regEntry.assimilate(intoRequestId);
            }
        });
    }

    /**
     * Marks the request as having reached the queue.
     *
     * @param event the request to mark
     */
    void requestQueued(AbstractBuildRequestEvent event)
    {
        transition(event, "Queued", new UnaryProcedure<RegEntry>()
        {
            public void process(RegEntry regEntry)
            {
                regEntry.queue();
            }
        });
    }

    /**
     * Marks the request as explicitly cancelled.
     *
     * @param event the request to mark
     */
    void requestCancelled(AbstractBuildRequestEvent event)
    {
        transition(event, "Cancelled", new UnaryProcedure<RegEntry>()
        {
            public void process(RegEntry regEntry)
            {
                regEntry.cancel();
            }
        });
    }

    /**
     * Marks the request as having been activated, with the given build number.
     *
     * @param event       the request to mark
     * @param buildNumber number of the build launched by the request
     */
    void requestActivated(AbstractBuildRequestEvent event, final long buildNumber)
    {
        transition(event, "Activated", new UnaryProcedure<RegEntry>()
        {
            public void process(RegEntry regEntry)
            {
                regEntry.activate(buildNumber);
            }
        });
    }

    private void transition(AbstractBuildRequestEvent event, String type, UnaryProcedure<RegEntry> p)
    {
        {
            lock.lock();
            try
            {
                RegEntry entry = lookupEntry(event.getId());
                if (entry == null)
                {
                    LOG.warning(type + " notification for unknown build request " + event.getId() + " project " + event.getProjectConfig().getName());
                }
                else
                {
                    p.process(entry);
                    lockCondition.signalAll();
                }
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    /**
     * Waits for a limited time for a request to be handled, or reach a state
     * from which it will never become handled.  A handled request is one that
     * has reached at least {@link RequestStatus#QUEUED} state.  If the timeout
     * is reached while the request is not handled, this method will return
     * {@link RequestStatus#UNHANDLED}.
     *
     * @param eventId       id of the event to wait for
     * @param timeoutMillis maximum number of milliseconds to wait
     * @return the status of the request
     */
    public RequestStatus waitForRequestToBeHandled(long eventId, long timeoutMillis)
    {
        return waitForRequestIn(eventId, timeoutMillis, RequestStatus.UNKNOWN, RequestStatus.REJECTED, RequestStatus.ASSIMILATED, RequestStatus.QUEUED, RequestStatus.CANCELLED, RequestStatus.ACTIVATED);
    }

    /**
     * Waits for a limited time for a request to be activated, or reach a state
     * from which it will never become activated.  An activated request is one
     * that has reached at the {@link RequestStatus#ACTIVATED} state.  If the
     * timeout is reached while the request is not activated, this method will
     * return either {@link RequestStatus#UNHANDLED} or
     * {@link RequestStatus#QUEUED}.
     *
     * @param eventId       id of the event to wait for
     * @param timeoutMillis maximum number of milliseconds to wait
     * @return the status of the request
     */
    public RequestStatus waitForRequestToBeActivated(long eventId, long timeoutMillis)
    {
        return waitForRequestIn(eventId, timeoutMillis, RequestStatus.UNKNOWN, RequestStatus.REJECTED, RequestStatus.ASSIMILATED, RequestStatus.CANCELLED, RequestStatus.ACTIVATED);
    }

    private RequestStatus waitForRequestIn(long eventId, long timeoutMillis, RequestStatus... acceptableStates)
    {
        Date deadline = new Date(System.currentTimeMillis() + timeoutMillis);
        lock.lock();
        try
        {
            RequestStatus status = getStatus(eventId);
            while (!CollectionUtils.contains(acceptableStates, status))
            {
                try
                {
                    if (lockCondition.awaitUntil(deadline))
                    {
                        status = getStatus(eventId);
                    }
                    else
                    {
                        break;
                    }
                }
                catch (InterruptedException e)
                {
                    // Retry.
                }
            }

            return status;
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Returns the current status of the given request.
     *
     * @param eventId id of the request to get the status of
     * @return the status of the request
     */
    public RequestStatus getStatus(long eventId)
    {
        lock.lock();
        try
        {
            RegEntry entry = lookupEntry(eventId);
            if (entry == null)
            {
                return RequestStatus.UNKNOWN;
            }
            else
            {
                return entry.status;
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Returns a human-readable message indicating why a request was rejected.
     * Only sensible for requests in the {@link RequestStatus#REJECTED} state.
     *
     * @param eventId if of the request
     * @return a message indicating why the request was rejected
     */
    public String getRejectionReason(long eventId)
    {
        lock.lock();
        try
        {
            RegEntry entry = lookupEntry(eventId);
            return entry == null ? null : entry.rejectionReason;
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Returns the id of the existing request that a request was assimilated
     * into.  Only sensible for requests in the
     * {@link RequestStatus#ASSIMILATED} state.
     *
     * @param eventId if of the request
     * @return id of the request that the given request was assimilated into
     */
    public long getAssimilatedId(long eventId)
    {
        lock.lock();
        try
        {
            RegEntry entry = lookupEntry(eventId);
            return entry == null ? NONE : entry.assimilatedId;
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Returns the build number for an activated request.  Only sensible for
     * requests in the {@link RequestStatus#ACTIVATED} state.
     *
     * @param eventId if of the request
     * @return build number for the build activated by the request
     */
    public long getBuildNumber(long eventId)
    {
        lock.lock();
        try
        {
            RegEntry entry = lookupEntry(eventId);
            return entry == null ? NONE : entry.buildNumber;
        }
        finally
        {
            lock.unlock();
        }
    }

    private RegEntry lookupEntry(long eventId)
    {
        RegEntry entry = requestIdToEntry.get(eventId);
        if (entry != null)
        {
            accessManager.ensurePermission(AccessManager.ACTION_VIEW, entry.request.getProjectConfig());
        }

        return entry;
    }

    void setLimit(int limit)
    {
        this.limit = limit;
    }

    void setTrim(int trim)
    {
        this.trim = trim;
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }

    private static class RegEntry
    {
        private AbstractBuildRequestEvent request;
        private RequestStatus status = RequestStatus.UNHANDLED;
        private long buildNumber = NONE;
        private long assimilatedId = NONE;
        private String rejectionReason;

        public RegEntry(AbstractBuildRequestEvent request)
        {
            this.request = request;
        }

        public void reject(String reason)
        {
            status = RequestStatus.REJECTED;
            this.rejectionReason = reason;
        }

        public void assimilate(long intoRequestId)
        {
            status = RequestStatus.ASSIMILATED;
            this.assimilatedId = intoRequestId;
        }

        public void queue()
        {
            status = RequestStatus.QUEUED;
        }

        public void cancel()
        {
            status = RequestStatus.CANCELLED;
        }

        public void activate(long buildNumber)
        {
            status = RequestStatus.ACTIVATED;
            this.buildNumber = buildNumber;
        }
    }
}
