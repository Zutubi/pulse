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

public class MethodNamePrefixPredicateTest extends ZutubiTestCase
{
    private static final String PREFIX = "get";

    private Method get;
    private Method getSomething;
    private Method together;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        get = MethodHolder.class.getMethod("get");
        getSomething = MethodHolder.class.getMethod("getSomething");
        together = MethodHolder.class.getMethod("together");
    }

    public void testPrefixMatches()
    {
        MethodNamePrefixPredicate predicate = new MethodNamePrefixPredicate(PREFIX, false);
        assertTrue(predicate.apply(getSomething));
    }

    public void testPrefixDoesntMatch()
    {
        MethodNamePrefixPredicate predicate = new MethodNamePrefixPredicate(PREFIX, false);
        assertFalse(predicate.apply(together));
    }

    public void testExactMatchAllowed()
    {
        MethodNamePrefixPredicate predicate = new MethodNamePrefixPredicate(PREFIX, true);
        assertTrue(predicate.apply(get));
        assertTrue(predicate.apply(getSomething));
    }

    public void testExactMatchNotAllowed()
    {
        MethodNamePrefixPredicate predicate = new MethodNamePrefixPredicate(PREFIX, false);
        assertFalse(predicate.apply(get));
        assertTrue(predicate.apply(getSomething));
    }

    private static class MethodHolder
    {
        public void get()
        {

        }

        public void getSomething()
        {

        }

        public void together()
        {

        }
    }
}
