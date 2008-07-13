package com.zutubi.pulse;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.security.AccessManager;

import java.util.*;

/**
 * The build queue manages multiple build requests for a single project or
 * user, ensuring (with the guidance of the FatController) that a project
 * is not building in parallel with itself, and that users only have one
 * live personal build at a time.
 */
public class BuildQueue
{
    /**
     * Map from entity to queued requests for that entity.  The first item
     * in the list is the executing build for the entity.  Behind that are
     * queued requests for the entity.  The entity is either a project or a
     * user (for personal builds).
     */
    private Map<Object, List<AbstractBuildRequestEvent>> requests;
    private AccessManager accessManager;

    public BuildQueue()
    {
        this.requests = new HashMap<Object, List<AbstractBuildRequestEvent>>();
    }

    /**
     * Adds the request to the queue and returns true if it should be
     * executed immediately.
     *
     * @param event the request to add
     * @return true iff the project of the given request can be built now
     */
    public boolean buildRequested(AbstractBuildRequestEvent event)
    {
        Object owner = event.getOwner();
        checkOwner(owner);

        List<AbstractBuildRequestEvent> entityRequests = requests.get(owner);
        if (entityRequests.size() > 0)
        {
            enqueueRequest(entityRequests, event);
            return false;
        }
        else
        {
            entityRequests.add(event);
            return true;
        }
    }

    private void checkOwner(Object owner)
    {
        if (!requests.containsKey(owner))
        {
            requests.put(owner, new LinkedList<AbstractBuildRequestEvent>());
        }
    }

    private void enqueueRequest(List<AbstractBuildRequestEvent> entityRequests, AbstractBuildRequestEvent event)
    {
        // Fixed revisions are always accepted, floating will be filtered out
        // if there is already a floater for the same project.
        if (!event.getRevision().isFixed())
        {
            // Include the running build, it can be floating until the first
            // recipe is dispatched (CIB-701).
            for (AbstractBuildRequestEvent e : entityRequests)
            {
                if (!e.getRevision().isFixed() && e.getProjectConfig().equals(event.getProjectConfig()))
                {
                    // Existing floater, no need to remember this request.
                    return;
                }
            }
        }

        // Unique spec, add to end of queue.
        entityRequests.add(event);
    }

    /**
     * Dequeues the completed request and returns the next request event for
     * the given entity.
     *
     * @param owner owner of the completed build
     * @return the next request for the project, or null if there is none
     */
    public AbstractBuildRequestEvent buildCompleted(Entity owner)
    {
        List<AbstractBuildRequestEvent> entityRequests = requests.get(owner);
        assert(entityRequests.size() > 0);
        entityRequests.remove(0);

        if (entityRequests.size() > 0)
        {
            return entityRequests.get(0);
        }
        else
        {
            return null;
        }
    }

    public Map<Object, List<AbstractBuildRequestEvent>> takeSnapshot()
    {
        Map<Object, List<AbstractBuildRequestEvent>> queue = new HashMap<Object, List<AbstractBuildRequestEvent>>();
        for (Map.Entry<Object, List<AbstractBuildRequestEvent>> entry : requests.entrySet())
        {
            List<AbstractBuildRequestEvent> events = new LinkedList<AbstractBuildRequestEvent>(entry.getValue());
            queue.put(entry.getKey(), events);
        }

        return queue;
    }

    public boolean cancelBuild(long id)
    {
        // Locate build request and remove it.  If it does not exist, return false.
        for (Map.Entry<Object, List<AbstractBuildRequestEvent>> entry : requests.entrySet())
        {
            List<AbstractBuildRequestEvent> events = entry.getValue();
            // Ignore the first in the list: it is already running
            Iterator<AbstractBuildRequestEvent> it = events.iterator();
            if(it.hasNext())
            {
                it.next();
                while(it.hasNext())
                {
                    if (it.next().getId() == id)
                    {
                        AbstractBuildRequestEvent event = it.next();
                        if (event.getId() == id)
                        {
                            accessManager.ensurePermission(ProjectConfigurationActions.ACTION_CANCEL_BUILD, event);
                            it.remove();
                            return true;
                        }

                        it.remove();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }
}
