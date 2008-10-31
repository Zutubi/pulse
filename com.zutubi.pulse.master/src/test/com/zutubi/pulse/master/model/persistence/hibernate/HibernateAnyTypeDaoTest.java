/**
 * <class-comment/>
 */
package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.persistence.AnyTypeDao;
import com.zutubi.pulse.master.model.persistence.ObjectHandle;

import java.util.LinkedList;
import java.util.List;

public class HibernateAnyTypeDaoTest extends MasterPersistenceTestCase
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
        entities.add(new User());
        List<ObjectHandle> expectedHandles = new LinkedList<ObjectHandle>();
        for (Entity e : entities)
        {
            dao.save(e);
            expectedHandles.add(new ObjectHandle(e.getId(), e.getClass()));
        }

        List<ObjectHandle> handles = dao.findAll();
        assertEquals(4, handles.size());
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