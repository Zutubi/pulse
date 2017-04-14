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

import com.zutubi.i18n.types.TestBook;
import com.zutubi.i18n.types.TestSubClass;
import com.zutubi.util.junit.ZutubiTestCase;

public class ExtendedClassContextResolverTest extends ZutubiTestCase
{
    private ExtendedClassContextResolver resolver;

    protected void setUp() throws Exception
    {
        super.setUp();

        resolver = new ExtendedClassContextResolver();
    }

    public void testJavaLangObject()
    {
        String[] resolvedBundleNames = resolver.resolve(new ClassContext(Object.class));
        assertEquals(1, resolvedBundleNames.length);
        assertEquals("java/lang/Object", resolvedBundleNames[0]);
    }

    public void testBook()
    {
        String[] resolvedBundleNames = resolver.resolve(new ClassContext(TestBook.class));
        assertEquals(2, resolvedBundleNames.length);
        assertEquals("com/zutubi/i18n/types/TestBook", resolvedBundleNames[0]);
        assertEquals("java/lang/Object", resolvedBundleNames[1]);
    }

    public void testSubClass()
    {
        String[] resolvedBundleNames = resolver.resolve(new ClassContext(TestSubClass.class));
        assertEquals(4, resolvedBundleNames.length);
        assertEquals("com/zutubi/i18n/types/TestSubClass", resolvedBundleNames[0]);
        assertEquals("com/zutubi/i18n/types/TestClass", resolvedBundleNames[1]);
        assertEquals("com/zutubi/i18n/types/TestInterface", resolvedBundleNames[2]);
        assertEquals("java/lang/Object", resolvedBundleNames[3]);
    }
}
