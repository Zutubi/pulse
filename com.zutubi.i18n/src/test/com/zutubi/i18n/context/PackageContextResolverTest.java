package com.zutubi.i18n.context;

import com.zutubi.util.junit.ZutubiTestCase;

/**
 * <class-comment/>
 */
public class PackageContextResolverTest extends ZutubiTestCase
{
    private PackageContextResolver resolver;

    protected void setUp() throws Exception
    {
        super.setUp();

        resolver = new PackageContextResolver();
    }

    protected void tearDown() throws Exception
    {
        resolver = null;

        super.tearDown();
    }

    public void testJavaLangObject()
    {
        String[] resolvedBundleNames = resolver.resolve(new PackageContext(Object.class));
        assertEquals(3, resolvedBundleNames.length);
        assertEquals("java/lang/package", resolvedBundleNames[0]);
        assertEquals("java/package", resolvedBundleNames[1]);
        assertEquals("package", resolvedBundleNames[2]);
    }
}
