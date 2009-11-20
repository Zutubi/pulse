package com.zutubi.pulse.master.model;

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import static com.zutubi.pulse.master.model.ProjectPredicates.exists;
import static com.zutubi.pulse.master.model.ProjectPredicates.notExists;

public class ProjectPredicatesTest extends ZutubiTestCase
{
    public void testExistsForNull()
    {
        assertFalse(exists(null));
        assertTrue(notExists(null));
    }

    public void testExistsForOrphaned()
    {
        Project orphan = createOrphaned();
        assertFalse(exists(orphan));
        assertTrue(notExists(orphan));
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
        assertTrue(ProjectPredicates.concrete(project));
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
