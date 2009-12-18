package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.util.junit.ZutubiTestCase;

public class ProjectConfigurationTest extends ZutubiTestCase
{
    private long handles = 1;

    public void testIsDependentWithNoDependents()
    {
        ProjectConfiguration clientA = project("clientA");
        ProjectConfiguration clientB = project("clientB");
        assertFalse(clientA.isDependentOn(clientB));
    }

    public void testIsDependentWithDependencies()
    {
        ProjectConfiguration client = project("client");
        ProjectConfiguration libA = project("libA");
        ProjectConfiguration libB = project("libB");
        addDependency(client, libA, libB);
        assertTrue(client.isDependentOn(libA));
        assertTrue(client.isDependentOn(libB));
        assertFalse(libA.isDependentOn(libB));
    }

    public void testIsDependentWithTransitiveDependencies()
    {
        ProjectConfiguration client = project("client");
        ProjectConfiguration lib = project("lib");
        ProjectConfiguration util = project("util");
        addDependency(client, lib);
        addDependency(lib, util);
        assertTrue(client.isDependentOn(lib));
        assertTrue(client.isDependentOn(util));
        assertTrue(lib.isDependentOn(util));
    }

    private ProjectConfiguration project(String name)
    {
        ProjectConfiguration project = new ProjectConfiguration(name);
        project.setHandle(handles++);
        return project;
    }

    private void addDependency(ProjectConfiguration project, ProjectConfiguration... others)
    {
        for (ProjectConfiguration other : others)
        {
            DependencyConfiguration dependency = new DependencyConfiguration();
            dependency.setProject(other);
            dependency.setHandle(handles++);
            project.getDependencies().getDependencies().add(dependency);
        }
    }
}
