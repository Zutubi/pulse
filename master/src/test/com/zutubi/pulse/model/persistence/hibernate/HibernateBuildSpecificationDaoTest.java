package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;
import com.zutubi.pulse.model.persistence.SlaveDao;

import java.util.Arrays;
import java.util.List;


/**
 * 
 *
 */
public class HibernateBuildSpecificationDaoTest extends MasterPersistenceTestCase
{
    private BuildSpecificationDao buildSpecificationDao;
    private SlaveDao slaveDao;

    public void setUp() throws Exception
    {
        super.setUp();
        buildSpecificationDao = (BuildSpecificationDao) context.getBean("buildSpecificationDao");
        slaveDao = (SlaveDao) context.getBean("slaveDao");
    }

    public void tearDown() throws Exception
    {
        slaveDao = null;
        buildSpecificationDao = null;

        try
        {
            super.tearDown();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void testSaveAndLoad()
    {
        BuildSpecification spec = assembleSpec();
        spec.markForCleanBuild(Arrays.asList(slaveDao.findByName("test slave")));
        buildSpecificationDao.save(spec);
        commitAndRefreshTransaction();

        BuildSpecification anotherSpec = buildSpecificationDao.findById(spec.getId());
        assertPropertyEquals(spec, anotherSpec);
    }

    public void testFindBySlave()
    {
        BuildSpecification spec = assembleSpec();
        BuildSpecification emptySpec = new BuildSpecification();
        buildSpecificationDao.save(spec);
        buildSpecificationDao.save(emptySpec);
        commitAndRefreshTransaction();

        Slave slave = slaveDao.findByName("test slave");
        List<BuildSpecification> specs= buildSpecificationDao.findBySlave(slave);

        assertEquals(1, specs.size());
        assertEquals(spec.getId(), specs.get(0).getId());
    }

    public void testFindBySlaveMarkedClean()
    {
        BuildSpecification spec = new BuildSpecification();
        Slave slave = insertSlave();
        spec.markForCleanBuild(Arrays.asList(slave));
        BuildSpecification emptySpec = new BuildSpecification();
        buildSpecificationDao.save(spec);
        buildSpecificationDao.save(emptySpec);
        commitAndRefreshTransaction();

        List<BuildSpecification> specs= buildSpecificationDao.findBySlave(slave);

        assertEquals(1, specs.size());
        assertEquals(spec.getId(), specs.get(0).getId());
    }

    private BuildSpecification assembleSpec()
    {
        BuildSpecification spec = new BuildSpecification("test spec");

        spec.setTimeout(100);
        BuildSpecificationNode masterNode = new BuildSpecificationNode(new BuildStage("parent", new MasterBuildHostRequirements(), "recipe 1"));
        masterNode.addResourceRequirement(new ResourceRequirement("resource", "version"));
        spec.getRoot().addChild(masterNode);

        Slave slave = insertSlave();
        BuildSpecificationNode slaveNode = new BuildSpecificationNode(new BuildStage("child", new SlaveBuildHostRequirements(slave), "recipe 2"));
        masterNode.addChild(slaveNode);
        return spec;
    }

    private Slave insertSlave()
    {
        Slave slave = new Slave("test slave", "test host");
        slaveDao.save(slave);
        return slave;
    }

}
