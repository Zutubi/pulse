package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.AntPulseFileDetails;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.DirectoryCapture;
import com.zutubi.pulse.model.FileCapture;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.RunExecutablePostBuildAction;
import com.zutubi.pulse.model.TagPostBuildAction;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;
import com.zutubi.pulse.model.persistence.ProjectDao;

import java.util.Arrays;
import java.util.List;

/**
 * 
 *
 */
public class HibernateProjectDaoTest extends MasterPersistenceTestCase
{
    private ProjectDao projectDao;
    private BuildSpecificationDao buildSpecificationDao;

    public void setUp() throws Exception
    {
        super.setUp();
        projectDao = (ProjectDao) context.getBean("projectDao");
        buildSpecificationDao = (BuildSpecificationDao) context.getBean("buildSpecificationDao");
    }

    public void tearDown() throws Exception
    {
        projectDao = null;
        buildSpecificationDao = null;
        super.tearDown();
    }

    public void testLoadSave()
    {
        Project project = new Project();
        project.setLastPollTime((long)33442);
        project.pause();

        projectDao.save(project);

        commitAndRefreshTransaction();

        Project otherProject = projectDao.findById(project.getId());
        assertPropertyEquals(project, otherProject);
    }

    public void testLoadSaveFileCapture()
    {
        AntPulseFileDetails details = new AntPulseFileDetails();
        FileCapture capture = new FileCapture("name", "file", "type");
        capture.addProcessor("processor");
        details.addCapture(capture);

        projectDao.save(details);
        commitAndRefreshTransaction();

        AntPulseFileDetails otherDetails = projectDao.findAntPulseFileSource(details.getId());
        assertPropertyEquals(capture, otherDetails.getCaptures().get(0));
    }

    public void testLoadSaveDirectoryCapture()
    {
        AntPulseFileDetails details = new AntPulseFileDetails();
        DirectoryCapture capture = new DirectoryCapture("name", "file", "type");
        capture.setIncludes("include pattern");
        capture.setIncludes("exclude pattern");
        capture.addProcessor("processor");
        details.addCapture(capture);

        projectDao.save(details);
        commitAndRefreshTransaction();

        AntPulseFileDetails otherDetails = projectDao.findAntPulseFileSource(details.getId());
        assertPropertyEquals(capture, otherDetails.getCaptures().get(0));
    }

    public void testLoadSaveTagAction()
    {
        BuildSpecification spec = new BuildSpecification("test");
        buildSpecificationDao.save(spec);

        TagPostBuildAction action = new TagPostBuildAction();
        action.setSpecifications(Arrays.asList(spec));
        action.setStates(Arrays.asList(ResultState.SUCCESS));
        action.setTag("tag-name");
        action.setMoveExisting(true);
        projectDao.save(action);
        commitAndRefreshTransaction();

        TagPostBuildAction otherAction = projectDao.findTagPostBuildAction(action.getId());
        assertPropertyEquals(action, otherAction);
    }

    public void testLoadSaveExecutableAction()
    {
        BuildSpecification spec = new BuildSpecification("test");
        buildSpecificationDao.save(spec);

        RunExecutablePostBuildAction action = new RunExecutablePostBuildAction();
        action.setSpecifications(Arrays.asList(spec));
        action.setStates(Arrays.asList(ResultState.SUCCESS));
        action.setCommand("command");
        action.setArguments("args");
        projectDao.save(action);
        commitAndRefreshTransaction();

        RunExecutablePostBuildAction otherAction = projectDao.findRunExecutablePostBuildAction(action.getId());
        assertPropertyEquals(action, otherAction);
    }

    public void testFindByName()
    {
        Project projectA = new Project("nameA");
        projectDao.save(projectA);
        commitAndRefreshTransaction();
        assertNotNull(projectDao.findByName("nameA"));
    }

    public void testFindByBuildSpecification()
    {
        Project p1 = new Project();
        Project p2 = new Project();
        BuildSpecification spec1 = new BuildSpecification();
        BuildSpecification spec2 = new BuildSpecification();
        p1.addBuildSpecification(spec1);
        p2.addBuildSpecification(spec2);
        projectDao.save(p1);
        projectDao.save(p2);
        commitAndRefreshTransaction();

        // assert that we get the right project back.
        Project found = projectDao.findByBuildSpecification(spec1);
        assertNotNull(found);
        assertEquals(p1.getId(), found.getId());
    }

    public void testFindByAdminAuthority()
    {
        addAdminProject("justA1");
        addAdminProject("justA1", "A1");
        addAdminProject("justA2", "A2");
        addAdminProject("bothA1A2", "A1", "A2");

        commitAndRefreshTransaction();
        List<Project> projects = projectDao.findByAdminAuthority("A1");
        assertEquals(2, projects.size());
        assertEquals("justA1", projects.get(0).getName());
        assertEquals("bothA1A2", projects.get(1).getName());
    }

    private Project addAdminProject(String name, String... authorities)
    {
        Project p = new Project(name);
        for(String a: authorities)
        {
            p.addAdmin(a);
        }
        projectDao.save(p);
        return p;
    }
}

