package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.spring.SpringObjectFactory;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import org.hibernate.criterion.Projections;

public class HibernateSearchQueriesTest extends MasterPersistenceTestCase
{
    private HibernateSearchQueries queries;
    private BuildResultDao buildResultDao;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        SpringObjectFactory objectFactory = new SpringObjectFactory();

        queries = new HibernateSearchQueries();
        queries.setObjectFactory(objectFactory);

        buildResultDao = (BuildResultDao) context.getBean("buildResultDao");
    }

    public void testBuildResultQuery()
    {
        HibernateSearchQuery<BuildResult> query = queries.getBuildResults();
        assertEquals(0, query.count());

        buildResultDao.save(new BuildResult());
        assertEquals(1, query.count());
    }

    public void testIdQuery()
    {
        HibernateSearchQuery<Long> query = queries.getIds(BuildResult.class);
        assertEquals(0, query.count());

        buildResultDao.save(new BuildResult());
        query.setProjection(Projections.id());
        assertEquals(1, query.count());
    }
}
