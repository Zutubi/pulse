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

package com.zutubi.i18n.context;

import com.zutubi.util.junit.ZutubiTestCase;

/**
 * <class-comment/>
 */
public class ClassContextResolverTest extends ZutubiTestCase
{
    private ClassContextResolver resolver;

    protected void setUp() throws Exception
    {
        super.setUp();

        resolver = new ClassContextResolver();
    }

    protected void tearDown() throws Exception
    {
        resolver = null;

        super.tearDown();
    }

    public void testJavaLangObject()
    {
        String[] resolvedBundleNames = resolver.resolve(new ClassContext(Object.class));
        assertEquals(1, resolvedBundleNames.length);
        assertEquals("java/lang/Object", resolvedBundleNames[0]);
    }

    public void testClassContext()
    {
        String[] resolvedBundleNames = resolver.resolve(new ClassContext(ClassContext.class));
        assertEquals(3, resolvedBundleNames.length);
        assertEquals("com/zutubi/i18n/context/ClassContext", resolvedBundleNames[0]);
        assertEquals("com/zutubi/i18n/context/Context", resolvedBundleNames[1]);
        assertEquals("java/lang/Object", resolvedBundleNames[2]);
    }
}
