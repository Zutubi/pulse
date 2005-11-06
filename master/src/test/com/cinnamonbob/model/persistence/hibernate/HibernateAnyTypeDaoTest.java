/**
 * <class-comment/>
 */
package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.model.BuildResult;
import com.cinnamonbob.core.model.Entity;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.User;
import com.cinnamonbob.model.persistence.AnyTypeDao;
import com.cinnamonbob.model.persistence.ObjectHandle;

import java.util.LinkedList;
import java.util.List;

public class HibernateAnyTypeDaoTest extends PersistenceTestCase
{
    private AnyTypeDao dao;

    public HibernateAnyTypeDaoTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        dao = (AnyTypeDao) context.getBean("anyTypeDao");

    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        dao = null;
        super.tearDown();
    }

    public void test()
    {
        List<Entity> entities = new LinkedList<Entity>();
        entities.add(new Project());
        entities.add(new Project());
        entities.add(new User());
        entities.add(new BuildResult());
        entities.add(new BuildResult());
        List<ObjectHandle> expectedHandles = new LinkedList<ObjectHandle>();
        for (Entity e : entities)
        {
            dao.save(e);
            expectedHandles.add(new ObjectHandle(e.getId(), e.getClass()));
        }

        List<ObjectHandle> handles = dao.findAll();
        assertEquals(5, handles.size());
        for (ObjectHandle handle : handles)
        {
            assertTrue(hasEntry(expectedHandles, handle));
        }
    }

    private boolean hasEntry(List l, Object o1)
    {
        for (Object o2 : l)
        {
            if (o2.equals(o1))
            {
                return true;
            }
        }
        return false;
    }
}