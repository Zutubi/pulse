package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.test.PulseTestCase;

public class NamedEntityComparatorTest extends PulseTestCase
{
    private NamedEntityComparator comparator = new NamedEntityComparator();

    public void testFirstLess()
    {
        assertTrue(comparator.compare(createEntity("a"), createEntity("b")) < 0);
    }

    public void testEqual()
    {
        assertEquals(0, comparator.compare(createEntity("a"), createEntity("a")));
    }

    public void testFirstGreater()
    {
        assertTrue(comparator.compare(createEntity("b"), createEntity("a")) > 0);
    }

    public void testFirstNull()
    {
        assertTrue(comparator.compare(createEntity(null), createEntity("a")) < 0);
    }

    public void testSecondNull()
    {
        assertTrue(comparator.compare(createEntity("a"), createEntity(null)) > 0);
    }

    public void testBothNull()
    {
        assertEquals(0, comparator.compare(createEntity(null), createEntity(null)));        
    }

    private NamedEntity createEntity(final String name)
    {
        return new NamedEntity()
        {
            public long getId()
            {
                return 0;
            }

            public String getName()
            {
                return name;
            }
        };
    }
}
