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
