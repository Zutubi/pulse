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

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectResponsibility;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.persistence.ProjectDao;
import com.zutubi.pulse.master.model.persistence.UserDao;

import java.util.List;

public class HibernateProjectDaoTest extends MasterPersistenceTestCase
{
    private ProjectDao projectDao;
    private UserDao userDao;

    public void setUp() throws Exception
    {
        super.setUp();
        projectDao = (ProjectDao) context.getBean("projectDao");
        userDao = (UserDao) context.getBean("userDao");
    }

    public void testLoadSave()
    {
        Project project = new Project();
        project.setLastPollTime((long)33442);

        projectDao.save(project);

        commitAndRefreshTransaction();

        Project otherProject = projectDao.findById(project.getId());
        assertPropertyEquals(project, otherProject);
    }

    public void testFindByResponsible()
    {
        User u1 = new User();
        User u2 = new User();
        User u3 = new User();
        userDao.save(u1);
        userDao.save(u2);
        userDao.save(u3);

        Project p1 = new Project();
        p1.setResponsibility(new ProjectResponsibility(u1, null));
        Project p2 = new Project();
        Project p3 = new Project();
        p3.setResponsibility(new ProjectResponsibility(u2, "yep"));
        projectDao.save(p1);
        projectDao.save(p2);
        projectDao.save(p3);

        commitAndRefreshTransaction();

        List<Project> projects = projectDao.findByResponsible(u1);
        assertEquals(1, projects.size());
        assertEquals(u1, projects.get(0).getResponsibility().getUser());

        projects = projectDao.findByResponsible(u2);
        assertEquals(1, projects.size());
        assertEquals(u2, projects.get(0).getResponsibility().getUser());

        projects = projectDao.findByResponsible(u3);
        assertEquals(0, projects.size());
    }

}

