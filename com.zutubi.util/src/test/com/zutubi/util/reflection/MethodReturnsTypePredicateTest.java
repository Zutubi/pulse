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

package com.zutubi.util.reflection;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.List;

public class MethodReturnsTypePredicateTest extends ZutubiTestCase
{
    // Just a sanity check as the logic is tested in ReflectionUtilsTest
    public void testSanity() throws NoSuchMethodException
    {
        MethodReturnsTypePredicate predicate = new MethodReturnsTypePredicate(List.class, String.class);
        assertTrue(predicate.apply(MethodHolder.class.getMethod("getStrings")));
        assertFalse(predicate.apply(MethodHolder.class.getMethod("getInts")));
        assertFalse(predicate.apply(MethodHolder.class.getMethod("getNothing")));
    }
    
    private static class MethodHolder
    {
        public List<String> getStrings()
        {
            return null;
        }

        public List<Integer> getInts()
        {
            return null;
        }

        public void getNothing()
        {
        }
    }
}