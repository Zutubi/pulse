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

import java.lang.reflect.Method;

public class MethodNameEqualsPredicateTest extends ZutubiTestCase
{
    private static final String NAME = "theName";

    private Method theName;
    private Method theNameSucks;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        theName = MethodHolder.class.getMethod("theName");
        theNameSucks = MethodHolder.class.getMethod("theNameSucks");
    }

    public void testNameMatches()
    {
        MethodNameEqualsPredicate predicate = new MethodNameEqualsPredicate(NAME);
        assertTrue(predicate.apply(theName));
    }

    public void testNameDoesntMatch()
    {
        MethodNameEqualsPredicate predicate = new MethodNameEqualsPredicate(NAME);
        assertFalse(predicate.apply(theNameSucks));
    }

    private static class MethodHolder
    {
        public void theName()
        {
        }

        public void theNameSucks()
        {
        }
    }
}