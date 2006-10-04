package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.BuildSpecificationNode;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.model.persistence.BuildSpecificationNodeDao;

import java.util.List;

/**
 *
 *
 */
public class HibernateBuildSpecificationNodeDaoTest extends MasterPersistenceTestCase
{
    private BuildSpecificationNodeDao buildSpecificationNodeDao;

    public void setUp() throws Exception
    {
        super.setUp();
        buildSpecificationNodeDao = (BuildSpecificationNodeDao) context.getBean("buildSpecificationNodeDao");
    }

    public void tearDown() throws Exception
    {
        buildSpecificationNodeDao = null;
        super.tearDown();
    }

    public void testFindByResource() throws Exception
    {
        BuildSpecificationNode node1 = new BuildSpecificationNode();
        node1.addResourceRequirement(new ResourceRequirement("r2", "v1"));
        node1.addResourceRequirement(new ResourceRequirement("r1", "v2"));
        BuildSpecificationNode node2 = new BuildSpecificationNode();
        node2.addResourceRequirement(new ResourceRequirement("r2", "v2"));
        BuildSpecificationNode node3 = new BuildSpecificationNode();
        BuildSpecificationNode node4 = new BuildSpecificationNode();
        node4.addResourceRequirement(new ResourceRequirement("r1", ""));

        buildSpecificationNodeDao.save(node1);
        buildSpecificationNodeDao.save(node2);
        buildSpecificationNodeDao.save(node3);
        buildSpecificationNodeDao.save(node4);

        commitAndRefreshTransaction();

        List<BuildSpecificationNode> nodes = buildSpecificationNodeDao.findByResourceRequirement("r1");
        assertEquals(2, nodes.size());
        assertEquals(node1, nodes.get(0));
        assertEquals(node4, nodes.get(1));
    }

}
