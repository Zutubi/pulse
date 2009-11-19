package com.zutubi.pulse.master.model;

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import static com.zutubi.pulse.master.model.ProjectFilters.exists;
import static com.zutubi.pulse.master.model.ProjectFilters.notExists;

public class ProjectFiltersTest extends ZutubiTestCase
{
    public void testExistsForNull()
    {
        assertFalse(exists(null));
        assertTrue(notExists(null));
    }

    public void testExistsForOrphaned()
    {
        Project orphane = createOrphaned();
        assertFalse(exists(orphane));
        assertTrue(notExists(orphane));
    }

    public void testExistsForExistingProject()
    {
        Project p = new Project();
        p.setConfig(new ProjectConfiguration());
        assertTrue(exists(p));
        assertFalse(notExists(p));
    }

    public void testConcrete()
    {
        ProjectConfiguration project = createConcrete();
        assertTrue(ProjectFilters.concrete(project));
    }

    private Project createOrphaned()
    {
        return new Project();
    }

    private ProjectConfiguration createConcrete()
    {
        ProjectConfiguration concreteProject = new ProjectConfiguration();
        concreteProject.setConcrete(true);
        return concreteProject;
    }
}
