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
