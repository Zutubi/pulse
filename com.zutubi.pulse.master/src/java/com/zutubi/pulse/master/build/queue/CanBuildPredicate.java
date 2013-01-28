package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Predicate;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;

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

    public boolean apply(RequestHolder holder)
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
