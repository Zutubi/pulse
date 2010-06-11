package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.spring.SpringObjectFactory;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.persistence.hibernate.MasterPersistenceTestCase;
import junit.framework.Assert;

public class HibernateSearchQueriesTest extends MasterPersistenceTestCase
{
    private HibernateSearchQueries queries;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        SpringObjectFactory objectFactory = new SpringObjectFactory();

        queries = new HibernateSearchQueries();
        queries.setObjectFactory(objectFactory);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testBuildResultQuery()
    {
        HibernateSearchQuery<BuildResult> query = queries.getBuildResults();
        assertEquals(0, query.count());
    }

    public void testIdQuery()
    {
        HibernateSearchQuery<Long> query = queries.getIds(BuildResult.class);
        assertEquals(0, query.count());
    }

    public void testStringQuery()
    {
        HibernateSearchQuery<String> query = queries.getStrings(BuildResult.class);
        assertEquals(0, query.count());
    }
}
