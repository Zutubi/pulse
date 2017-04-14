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

import com.zutubi.pulse.master.model.LabelProjectTuple;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.persistence.UserDao;

import java.util.List;

public class HibernateUserDaoTest extends MasterPersistenceTestCase
{
    private UserDao userDao;

    public void setUp() throws Exception
    {
        super.setUp();
        userDao = (UserDao) context.getBean("userDao");
    }

    public void testSaveAndLoad()
    {
        User user = new User();
        user.setEnabled(true);
        user.getBrowseViewCollapsed().add(new LabelProjectTuple("label", 123));
        user.getDashboardCollapsed().add(new LabelProjectTuple("", 0));
        userDao.save(user);
        commitAndRefreshTransaction();

        User anotherUser = userDao.findById(user.getId());

        // want to ensure that a new object has been created by hibernate and
        // that the old one is not just returned to us.
        assertFalse(user == anotherUser);
        assertPropertyEquals(user, anotherUser);
    }

    public void testFindAll()
    {
        User user = new User();
        userDao.save(user);
        commitAndRefreshTransaction();

        List users = userDao.findAll();
        assertNotNull(users);
        assertEquals(1, users.size());
    }
}
