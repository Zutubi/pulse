/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
