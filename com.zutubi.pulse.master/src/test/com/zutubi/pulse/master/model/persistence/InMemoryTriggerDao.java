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

package com.zutubi.pulse.master.model.persistence;


import com.google.common.base.Predicate;
import com.zutubi.pulse.master.scheduling.Trigger;

import java.util.List;

public class InMemoryTriggerDao extends InMemoryEntityDao<Trigger> implements TriggerDao
{
    public List<Trigger> findByGroup(final String group)
    {
        return findByPredicate(new Predicate<Trigger>()
        {
            public boolean apply(Trigger trigger)
            {
                return group.compareTo(trigger.getGroup()) == 0;
            }
        });
    }

    public Trigger findByNameAndGroup(final String name, final String group)
    {
        return findUniqueByPredicate(new Predicate<Trigger>()
        {
            public boolean apply(Trigger trigger)
            {
                return group.compareTo(trigger.getGroup()) == 0 &&
                        name.compareTo(trigger.getName()) == 0;
            }
        });
    }

    public List<Trigger> findByProject(final long id)
    {
        return findByPredicate(new Predicate<Trigger>()
        {
            public boolean apply(Trigger trigger)
            {
                return trigger.getProject() == id;
            }
        });
    }

    public Trigger findByProjectAndName(final long id, final String name)
    {
        return findUniqueByPredicate(new Predicate<Trigger>()
        {
            public boolean apply(Trigger trigger)
            {
                return name.compareTo(trigger.getName()) == 0 &&
                        id == trigger.getProject();
            }
        });
    }
}
