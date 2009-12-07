package com.zutubi.pulse.master.build.queue;

import com.zutubi.util.Predicate;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;

/**
 * Predicate that is satisfied if the project state accepts a
 * build being triggered by the request.
 *
 * @param <T>
 *
 * @see com.zutubi.pulse.master.model.Project.State#acceptTrigger(boolean)
 */
public class CanBuildPredicate<T extends RequestHolder> implements Predicate<T>
{
    private ProjectManager projectManager;

    public boolean satisfied(RequestHolder holder)
    {
        BuildRequestEvent request = holder.getRequest();
        Project project = projectManager.getProject(request.getProjectConfig().getProjectId(), false);
        return project != null && project.getState().acceptTrigger(request.isPersonal());
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
