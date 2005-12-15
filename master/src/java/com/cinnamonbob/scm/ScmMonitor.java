package com.cinnamonbob.scm;

import com.cinnamonbob.model.Scm;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.core.event.EventManager;

import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public class ScmMonitor
{
    private Map<Scm, Revision> previousRevisions = new HashMap<Scm, Revision>();

    private ProjectManager projectManager;
    private EventManager eventManager;

    public void check() throws SCMException
    {
        for (Project project : projectManager.getAllProjects())
        {
            for (Scm scm : project.getScms())
            {
                if (hasChanged(scm))
                {
                    Revision revision = previousRevisions.get(scm);
                    SCMChangeEvent event = new SCMChangeEvent(scm, revision);
                    eventManager.publish(event);
                }
            }
        }
    }

    public boolean hasChanged(Scm scm) throws SCMException
    {
        SCMServer scmServer = scm.createServer();
        if (!previousRevisions.containsKey(scm))
        {
            previousRevisions.put(scm, scmServer.getLatestRevision());
        }
        Revision previousRevision = previousRevisions.get(scm);
        if (scmServer.hasChangedSince(previousRevision))
        {
            previousRevisions.put(scm, scmServer.getLatestRevision());
            return true;
        }
        return false;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
