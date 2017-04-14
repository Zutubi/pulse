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

import com.zutubi.pulse.master.model.persistence.TriggerDao;
import com.zutubi.pulse.master.scheduling.Trigger;

import java.util.List;

/**
 * Hibernate implementation of {@link TriggerDao}.
 */
public class HibernateTriggerDao extends HibernateEntityDao<Trigger> implements TriggerDao
{
    public Class<Trigger> persistentClass()
    {
        return Trigger.class;
    }

    public List<Trigger> findByGroup(String group)
    {
        return findByNamedQuery("findByGroup", "group", group);
    }

    public List<Trigger> findByProject(long id)
    {
        return findByNamedQuery("findByProject", "project", id);
    }

    public Trigger findByProjectAndName(long id, String name)
    {
        return (Trigger) findUniqueByNamedQuery("findByProjectAndName", "project", id, "name", name);
    }

    public Trigger findByNameAndGroup(String name, String group)
    {
        return (Trigger) findUniqueByNamedQuery("findByNameAndGroup", "name", name, "group", group);
    }
}
