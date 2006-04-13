/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse;

import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.model.Project;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The project queue manages multiple build requests for a single project,
 * ensuring (with the guidance of the FatController) that a project is not
 * building in parallel with itself, but that all relevant requests for a
 * project (i.e. those for different specifications) are remembered.
 */
public class ProjectQueue
{
    /**
     * Map from project to queued requests for that project.  The first item
     * in the list is the executing build for the project.  Behind that are
     * queued requests for the project, at most one for each build
     * specification defined.
     */
    private Map<Project, List<BuildRequestEvent>> requests;

    public ProjectQueue()
    {
        this.requests = new HashMap<Project, List<BuildRequestEvent>>();
    }

    /**
     * Adds the request to the queue and returns true if it should be
     * executed immediately.
     *
     * @param event the request to add
     * @return true iff the project of the given request can be built now
     */
    public boolean buildRequested(BuildRequestEvent event)
    {
        Project project = event.getProject();
        checkProject(project);

        List<BuildRequestEvent> projectRequests = requests.get(project);
        if (projectRequests.size() > 0)
        {
            enqueueRequest(projectRequests, event);
            return false;
        }
        else
        {
            projectRequests.add(event);
            return true;
        }
    }

    private void checkProject(Project project)
    {
        if (!requests.containsKey(project))
        {
            requests.put(project, new LinkedList<BuildRequestEvent>());
        }
    }

    private void enqueueRequest(List<BuildRequestEvent> projectRequests, BuildRequestEvent event)
    {
        // Ignore the running build.
        for (int i = 1; i < projectRequests.size(); i++)
        {
            BuildRequestEvent e = projectRequests.get(i);
            if (e.getSpecification().equals(event.getSpecification()))
            {
                // This spec is already queued, no need to remember this
                // request.
                return;
            }
        }

        // Unique spec, add to end of queue.
        projectRequests.add(event);
    }

    /**
     * Dequeues the completed request and returns the next request event for
     * the given project.
     *
     * @param project the project to get the next request for
     * @return the next request for the project, or null if there is none
     */
    public BuildRequestEvent buildCompleted(Project project)
    {
        List<BuildRequestEvent> projectRequests = requests.get(project);
        assert(projectRequests.size() > 0);
        projectRequests.remove(0);

        if (projectRequests.size() > 0)
        {
            return projectRequests.get(0);
        }
        else
        {
            return null;
        }
    }

    public Map<Project, List<BuildRequestEvent>> takeSnapshot()
    {
        Map<Project, List<BuildRequestEvent>> queue = new HashMap<Project, List<BuildRequestEvent>>();
        for (Map.Entry<Project, List<BuildRequestEvent>> entry : requests.entrySet())
        {
            List<BuildRequestEvent> events = new LinkedList<BuildRequestEvent>(entry.getValue());
            queue.put(entry.getKey(), events);
        }

        return queue;
    }
}
