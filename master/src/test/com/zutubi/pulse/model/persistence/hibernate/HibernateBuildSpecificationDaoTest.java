/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;
import com.zutubi.pulse.model.persistence.SlaveDao;


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
        BuildSpecification spec = new BuildSpecification("test spec");

        spec.setTimeout(100);
        BuildSpecificationNode masterNode = new BuildSpecificationNode(new BuildStage("parent", new MasterBuildHostRequirements(), "recipe 1"));
        masterNode.addResourceRequirement(new ResourceRequirement("resource", "version"));
        spec.getRoot().addChild(masterNode);

        Slave slave = new Slave("test slave", "test host");
        slaveDao.save(slave);

        BuildSpecificationNode slaveNode = new BuildSpecificationNode(new BuildStage("child", new SlaveBuildHostRequirements(slave), "recipe 2"));
        masterNode.addChild(slaveNode);

        buildSpecificationDao.save(spec);
        commitAndRefreshTransaction();

        BuildSpecification anotherSpec = buildSpecificationDao.findById(spec.getId());
        assertPropertyEquals(spec, anotherSpec);
    }
}
