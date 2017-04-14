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
public class StaticPackageContextResolverTest extends ZutubiTestCase
{
    private StaticPackageContextResolver resolver;

    protected void setUp() throws Exception
    {
        super.setUp();

        resolver = new StaticPackageContextResolver();
    }

    protected void tearDown() throws Exception
    {
        resolver = null;

        super.tearDown();
    }

    public void testMatchingPackage()
    {
        resolver.addBundle("com.zutubi.i18n", "bundle");

        String[] resolvedBundleNames = resolver.resolve(new PackageContext("com.zutubi.i18n"));
        assertEquals(1, resolvedBundleNames.length);
        assertEquals("bundle", resolvedBundleNames[0]);
    }

    public void testHierarchicalPackage()
    {
        resolver.addBundle("com.zutubi.i18n", "bundle");

        String[] resolvedBundleNames = resolver.resolve(new PackageContext("com.zutubi.i18n.something"));
        assertEquals(1, resolvedBundleNames.length);
        assertEquals("bundle", resolvedBundleNames[0]);
    }

    public void testMultiplePackages()
    {
        resolver.addBundle("com.zutubi.i18n", "bundle");
        resolver.addBundle("com", "anotherbundle");

        String[] resolvedBundleNames = resolver.resolve(new PackageContext("com.zutubi.i18n"));
        assertEquals(2, resolvedBundleNames.length);
        assertEquals("bundle", resolvedBundleNames[0]);
        assertEquals("anotherbundle", resolvedBundleNames[1]);

    }
}
