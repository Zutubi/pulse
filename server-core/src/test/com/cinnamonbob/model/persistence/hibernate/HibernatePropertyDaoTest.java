package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.model.Property;
import com.cinnamonbob.model.persistence.PropertyDao;

/**
 *
 *
 */
public class HibernatePropertyDaoTest extends ServerCorePersistenceTestCase
{
    private PropertyDao propertyDao;

    public void setUp() throws Exception
    {
        super.setUp();
        propertyDao = (PropertyDao) context.getBean("propertyDao");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testLoadSave() throws Exception
    {
        Property p = new Property("test name", "test value");
        propertyDao.save(p);

        commitAndRefreshTransaction();

        Property otherP = propertyDao.findById(p.getId());
        assertPropertyEquals(p, otherP);
    }
}
