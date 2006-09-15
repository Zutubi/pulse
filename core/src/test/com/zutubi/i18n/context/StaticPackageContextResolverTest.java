package com.zutubi.i18n.context;

import junit.framework.TestCase;

/**
 * <class-comment/>
 */
public class StaticPackageContextResolverTest extends TestCase
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
        resolver.addBundle(new PackageContext("com.zutubi.i18n"), "bundle");

        String[] resolvedBundleNames = resolver.resolve(new PackageContext("com.zutubi.i18n"));
        assertEquals(1, resolvedBundleNames.length);
        assertEquals("bundle", resolvedBundleNames[0]);
    }

    public void testHierarchicalPackage()
    {
        resolver.addBundle(new PackageContext("com.zutubi.i18n"), "bundle");

        String[] resolvedBundleNames = resolver.resolve(new PackageContext("com.zutubi.i18n.something"));
        assertEquals(1, resolvedBundleNames.length);
        assertEquals("bundle", resolvedBundleNames[0]);
    }

    public void testMultiplePackages()
    {
        resolver.addBundle(new PackageContext("com.zutubi.i18n"), "bundle");
        resolver.addBundle(new PackageContext("com"), "anotherbundle");

        String[] resolvedBundleNames = resolver.resolve(new PackageContext("com.zutubi.i18n"));
        assertEquals(2, resolvedBundleNames.length);
        assertEquals("bundle", resolvedBundleNames[0]);
        assertEquals("anotherbundle", resolvedBundleNames[1]);

    }
}
