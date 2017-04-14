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

package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.logging.Logger;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.zutubi.util.StringUtils.capitalise;

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

    private static final int DEFAULT_LIMIT = 2000;
    private static final int DEFAULT_TRIM  = 200;

    /**
     * Limit on the number of remembered requests - when we hit this we prune
     * by the trim amount.
     */
    private int limit = DEFAULT_LIMIT;
    /**
     * Number of requests to prune when we hit the limit.
     */
    private int trim = DEFAULT_TRIM;

    private AccessManager accessManager;

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

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
    private ProjectManager projectManager;

    /**
     * Registers a new request with the registry.  Should be done before a
     * request event is published.
     *
     * @param event the new event to register
     */
    public void register(BuildRequestEvent event)
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
    public void requestRejected(BuildRequestEvent event, final String reason)
    {
        transition(event, RequestStatus.REJECTED, new UnaryProcedure<RegEntry>()
        {
            public void run(RegEntry regEntry)
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
    public void requestAssimilated(BuildRequestEvent event, final long intoRequestId)
    {
        transition(event, RequestStatus.ASSIMILATED, new UnaryProcedure<RegEntry>()
        {
            public void run(RegEntry regEntry)
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
    public void requestQueued(BuildRequestEvent event)
    {
        transition(event, RequestStatus.QUEUED, new UnaryProcedure<RegEntry>()
        {
            public void run(RegEntry regEntry)
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
    public void requestCancelled(BuildRequestEvent event)
    {
        transition(event, RequestStatus.CANCELLED, new UnaryProcedure<RegEntry>()
        {
            public void run(RegEntry regEntry)
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
    public void requestActivated(BuildRequestEvent event, final long buildNumber)
    {
        transition(event, RequestStatus.ACTIVATED, new UnaryProcedure<RegEntry>()
        {
            public void run(RegEntry regEntry)
            {
                regEntry.activate(buildNumber);
            }
        });
    }

    private void transition(BuildRequestEvent event, RequestStatus newStatus, UnaryProcedure<RegEntry> p)
    {
        lock.lock();
        try
        {
            RegEntry entry = lookupEntry(event.getId());
            if (entry == null)
            {
                LOG.warning(capitalise(newStatus.toString()) + " notification for unknown build request " + event.getId() + " project " + event.getProjectConfig().getName());
            }
            else
            {
                p.run(entry);
                lockCondition.signalAll();
            }
        }
        finally
        {
            lock.unlock();
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
                    status = getStatus(eventId);
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
            Project project = projectManager.getProject(entry.projectId, true);
            if (project == null)
            {
                throw new AccessDeniedException("Unable to access project");
            }
            
            accessManager.ensurePermission(AccessManager.ACTION_VIEW, project);
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
        private long projectId;
        private RequestStatus status = RequestStatus.UNHANDLED;
        private long buildNumber = NONE;
        private long assimilatedId = NONE;
        private String rejectionReason;

        public RegEntry(BuildRequestEvent request)
        {
            this.projectId = request.getProjectConfig().getProjectId();
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
