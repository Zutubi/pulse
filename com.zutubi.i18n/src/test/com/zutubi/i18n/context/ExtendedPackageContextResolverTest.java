package com.zutubi.i18n.context;

import junit.framework.TestCase;

import java.awt.*;

public class ExtendedPackageContextResolverTest extends TestCase
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
