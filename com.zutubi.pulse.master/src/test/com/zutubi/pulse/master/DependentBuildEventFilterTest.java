package com.zutubi.pulse.master;

import com.zutubi.events.Event;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.scheduling.TaskExecutionContext;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class DependentBuildEventFilterTest extends PulseTestCase
{
    private static final long TRIGGERED_PROJECT_ID = 0L;

    private DependentBuildEventFilter filter;
    private TaskExecutionContext context;
    private Trigger trigger;
    private ProjectManager projectManager;
    private BuildResult result;
    private Project builtProject;

    protected void setUp() throws Exception
    {
        super.setUp();

        projectManager = mock(ProjectManager.class);

        filter = new DependentBuildEventFilter();
        filter.setProjectManager(projectManager);

        trigger = mock(Trigger.class);
        stub(trigger.getProject()).toReturn(TRIGGERED_PROJECT_ID);
        stub(trigger.getConfig()).toReturn(new DependentBuildTriggerConfiguration());

        builtProject = mock(Project.class);

        result = mock(BuildResult.class);
        stub(result.getProject()).toReturn(builtProject);

        context = new TaskExecutionContext();
        context.setTrigger(trigger);
    }

    public void testAcceptSuccessfulBuild()
    {
        stub(result.succeeded()).toReturn(true);
        stub(builtProject.getId()).toReturn(1L);
        stub(projectManager.getProjectConfig(TRIGGERED_PROJECT_ID, false)).toReturn(projectWithDependencies(1L));
        assertTrue(filter.accept(trigger, newEvent(), context));
    }

    public void testRejectFailedBuild()
    {
        stub(result.succeeded()).toReturn(false);
        stub(builtProject.getId()).toReturn(1L);
        stub(projectManager.getProjectConfig(TRIGGERED_PROJECT_ID, false)).toReturn(projectWithDependencies(1L));
        assertFalse(filter.accept(trigger, newEvent(), context));
    }

    public void testRejectUnrelatedBuild()
    {
        stub(result.succeeded()).toReturn(true);
        stub(builtProject.getId()).toReturn(2L);
        stub(projectManager.getProjectConfig(TRIGGERED_PROJECT_ID, false)).toReturn(projectWithDependencies(1L));
        assertFalse(filter.accept(trigger, newEvent(), context));
    }

    public void testRejectUnexpectedEvent()
    {
        assertFalse(filter.accept(trigger, new Event(this), context));
    }

    private BuildCompletedEvent newEvent()
    {
        return new BuildCompletedEvent(this, result, null);
    }

    private ProjectConfiguration projectWithDependencies(long... ids)
    {
        ProjectConfiguration project = new ProjectConfiguration();
        for (long id : ids)
        {
            ProjectConfiguration dependencyProject = new ProjectConfiguration();
            dependencyProject.setProjectId(id);

            DependencyConfiguration dependency = new DependencyConfiguration();
            dependency.setProject(dependencyProject);

            project.getDependencies().getDependencies().add(dependency);
        }
        return project;
    }
}
