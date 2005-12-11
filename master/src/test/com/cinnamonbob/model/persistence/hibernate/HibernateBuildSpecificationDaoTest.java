package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.FileArtifact;
import com.cinnamonbob.core.model.*;
import com.cinnamonbob.model.*;
import com.cinnamonbob.model.persistence.BuildResultDao;
import com.cinnamonbob.model.persistence.SlaveDao;

import java.io.File;
import java.util.Calendar;


/**
 * 
 *
 */
public class HibernateBuildSpecificationDaoTest extends PersistenceTestCase
{
    private BuildSpecificationDao buildSpecificationDao;
    private SlaveDao slaveDao;

    public void setUp() throws Exception
    {
        super.setUp();
        buildSpecificationDao = (BuildSpecificationDao) context.getBean("buildSpecificationDao");
        slaveDao = (SlaveDao)context.getBean("slaveDao");
    }

    public void tearDown() throws Exception
    {
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

        BuildSpecificationNode masterNode = new BuildSpecificationNode(new MasterBuildServiceResolver(), "recipe 1");
        spec.addNode(masterNode);

        Slave slave = new Slave("test slave", "test host");
        slaveDao.save(slave);
        
        BuildSpecificationNode slaveNode = new BuildSpecificationNode(new SlaveBuildServiceResolver(slave), "recipe 2");
        masterNode.addChild(slaveNode);

        buildSpecificationDao.save(spec);
        commitAndRefreshTransaction();

        BuildSpecification anotherSpec = buildSpecificationDao.findById(spec.getId());
        assertPropertyEquals(spec, anotherSpec);
    }
}
