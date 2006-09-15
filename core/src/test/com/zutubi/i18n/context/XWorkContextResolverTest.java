package com.zutubi.i18n.context;

import junit.framework.TestCase;

/**
 * <class-comment/>
 */
public class XWorkContextResolverTest extends TestCase
{
    private XWorkContextResolver resolver;

    protected void setUp() throws Exception
    {
        super.setUp();

        resolver = new XWorkContextResolver();
    }

    protected void tearDown() throws Exception
    {
        resolver = null;

        super.tearDown();
    }

    public void testJavaLangObject()
    {
        String[] resolvedBundleNames = resolver.resolve(new XWorkContext(new Object()));
        assertEquals(4, resolvedBundleNames.length);
        assertEquals("java/lang/Object", resolvedBundleNames[0]);
        assertEquals("java/lang/package", resolvedBundleNames[1]);
        assertEquals("java/package", resolvedBundleNames[2]);
        assertEquals("package", resolvedBundleNames[3]);
    }
}
