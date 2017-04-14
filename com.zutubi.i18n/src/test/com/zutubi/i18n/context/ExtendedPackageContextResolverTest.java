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

import java.awt.*;

public class ExtendedPackageContextResolverTest extends ZutubiTestCase
{
    private ExtendedPackageContextResolver resolver = null;

    protected void setUp() throws Exception
    {
        super.setUp();
        resolver = new ExtendedPackageContextResolver();
    }

    protected void tearDown() throws Exception
    {
        resolver = null;
        super.tearDown();
    }

    public void testJavaLangObject()
    {
        String[] resolvedBundleNames = resolver.resolve(new ClassContext(Object.class));
        assertEquals(3, resolvedBundleNames.length);
        assertEquals("java/lang/package", resolvedBundleNames[0]);
        assertEquals("java/package", resolvedBundleNames[1]);
        assertEquals("package", resolvedBundleNames[2]);
    }

    public void testJavaAwtList()
    {
        String[] resolvedBundleNames = resolver.resolve(new ClassContext(List.class));
        assertEquals(8, resolvedBundleNames.length);
        assertEquals("java/awt/package", resolvedBundleNames[0]);
        assertEquals("java/package", resolvedBundleNames[1]);
        assertEquals("javax/accessibility/package", resolvedBundleNames[2]);
        assertEquals("javax/package", resolvedBundleNames[3]);
        assertEquals("java/awt/image/package", resolvedBundleNames[4]);
        assertEquals("java/io/package", resolvedBundleNames[5]);
        assertEquals("java/lang/package", resolvedBundleNames[6]);
        assertEquals("package", resolvedBundleNames[7]);
    }
}
