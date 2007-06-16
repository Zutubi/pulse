package com.zutubi.i18n.context;

import junit.framework.TestCase;
import com.zutubi.i18n.mock.MockBook;
import com.zutubi.i18n.mock.MockSubClass;

/**
 * <class-comment/>
 */
public class ExtendedClassContextResolverTest extends TestCase
{
    private ExtendedClassContextResolver resolver;

    protected void setUp() throws Exception
    {
        super.setUp();

        resolver = new ExtendedClassContextResolver();
    }

    protected void tearDown() throws Exception
    {
        resolver = null;

        super.tearDown();
    }

    public void testJavaLangObject()
    {
        String[] resolvedBundleNames = resolver.resolve(new ExtendedClassContext(Object.class));
        assertEquals(4, resolvedBundleNames.length);
        assertEquals("java/lang/Object", resolvedBundleNames[0]);
        assertEquals("java/lang/package", resolvedBundleNames[1]);
        assertEquals("java/package", resolvedBundleNames[2]);
        assertEquals("package", resolvedBundleNames[3]);
    }

    public void testMockBook()
    {
        String[] resolvedBundleNames = resolver.resolve(new ExtendedClassContext(MockBook.class));
        assertEquals(7, resolvedBundleNames.length);
        assertEquals("com/zutubi/i18n/mock/MockBook", resolvedBundleNames[0]);
        assertEquals("java/lang/Object", resolvedBundleNames[1]);
        assertEquals("com/zutubi/i18n/mock/package", resolvedBundleNames[2]);
        assertEquals("com/zutubi/i18n/package", resolvedBundleNames[3]);
        assertEquals("com/zutubi/package", resolvedBundleNames[4]);
        assertEquals("com/package", resolvedBundleNames[5]);
        assertEquals("package", resolvedBundleNames[6]);
    }

    public void testMockSubClass()
    {
        String[] resolvedBundleNames = resolver.resolve(new ExtendedClassContext(MockSubClass.class));
        assertEquals(9, resolvedBundleNames.length);
        assertEquals("com/zutubi/i18n/mock/MockSubClass", resolvedBundleNames[0]);
        assertEquals("com/zutubi/i18n/mock/MockClass", resolvedBundleNames[1]);
        assertEquals("com/zutubi/i18n/mock/MockInterface", resolvedBundleNames[2]);
        assertEquals("java/lang/Object", resolvedBundleNames[3]);
        assertEquals("com/zutubi/i18n/mock/package", resolvedBundleNames[4]);
        assertEquals("com/zutubi/i18n/package", resolvedBundleNames[5]);
        assertEquals("com/zutubi/package", resolvedBundleNames[6]);
        assertEquals("com/package", resolvedBundleNames[7]);
        assertEquals("package", resolvedBundleNames[8]);
    }
}
