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

package com.zutubi.util.adt;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;

public class ReverseListIteratorTest extends ZutubiTestCase
{
    public void testEmptyList()
    {
        assertOrdering(Arrays.<String>asList());
    }

    public void testSingleItemInList()
    {
        assertOrdering(asList("A"), "A");
    }
    
    public void testMultipleItemsInList()
    {
        assertOrdering(asList("A", "B"), "B", "A");
        assertOrdering(asList("A", "B", "C"), "C", "B", "A");
    }

    private void assertOrdering(List<String> list, String... expectedReverse)
    {
        Iterator<String> i = new ReverseListIterator<String>(list);
        for (String expected : expectedReverse)
        {
            assertTrue(i.hasNext());
            assertEquals(expected,  i.next());
        }
        assertFalse(i.hasNext());
    }
}
