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

package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

public class EntityComparatorTest extends PulseTestCase
{
    public void testOrdering()
    {
        List<Entity> entities = Arrays.asList(createEntity(3), createEntity(1), createEntity(4), createEntity(2));
        Collections.sort(entities, new EntityComparator<Entity>());
        assertEquals(1, entities.get(0).getId());
        assertEquals(2, entities.get(1).getId());
        assertEquals(3, entities.get(2).getId());
        assertEquals(4, entities.get(3).getId());
    }

    private Entity createEntity(long id)
    {
        Entity entity = new Entity();
        entity.setId(id);
        return entity;
    }
}
