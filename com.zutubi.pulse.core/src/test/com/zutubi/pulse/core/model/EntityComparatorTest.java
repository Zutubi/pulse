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
