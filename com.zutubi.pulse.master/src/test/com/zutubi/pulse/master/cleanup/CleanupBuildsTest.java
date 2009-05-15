package com.zutubi.pulse.master.cleanup;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.cleanup.requests.ProjectCleanupRequest;
import com.zutubi.util.bean.WiringObjectFactory;
import static org.mockito.Mockito.*;

import java.util.LinkedList;
import java.util.Arrays;

public class CleanupBuildsTest extends PulseTestCase
{
    private ProjectManager projectManager;
    private WiringObjectFactory objectFactory;
    private CleanupManager cleanupManager;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        projectManager = mock(ProjectManager.class);
        cleanupManager = mock(CleanupManager.class);
        objectFactory = new WiringObjectFactory();

        objectFactory.initProperties(this);
    }

    public void testNoProjects()
    {
        CleanupBuilds task = newTask();
        stub(projectManager.getProjects(false)).toReturn(new LinkedList<Project>());
        task.execute(null);

        verify(cleanupManager, times(0)).process(anyList());
    }

    public void testSomeProjects()
    {
        CleanupBuilds task = newTask();
        Project projectA = newProject(1);
        Project projectB = newProject(2);
        stub(projectManager.getProjects(false)).toReturn(Arrays.asList(projectA, projectB));
        task.execute(null);

        verify(cleanupManager, times(1)).process(Arrays.asList((CleanupRequest)new ProjectCleanupRequest(projectA), new ProjectCleanupRequest(projectB)));
    }

    private Project newProject(long id)
    {
        Project p = new Project();
        p.setId(id);
        return p;
    }

    private CleanupBuilds newTask()
    {
        CleanupBuilds task = new CleanupBuilds();
        task.setObjectFactory(objectFactory);
        task.setProjectManager(projectManager);
        task.setCleanupManager(cleanupManager);
        return task;
    }
}
