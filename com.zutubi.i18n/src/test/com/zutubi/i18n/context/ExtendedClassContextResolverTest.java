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
        String[] resolvedBundleNames = resolver.resolve(new ClassContext(Object.class));
        assertEquals(1, resolvedBundleNames.length);
        assertEquals("java/lang/Object", resolvedBundleNames[0]);
    }

    public void testMockBook()
    {
        String[] resolvedBundleNames = resolver.resolve(new ClassContext(MockBook.class));
        assertEquals(2, resolvedBundleNames.length);
        assertEquals("com/zutubi/i18n/mock/MockBook", resolvedBundleNames[0]);
        assertEquals("java/lang/Object", resolvedBundleNames[1]);
    }

    public void testMockSubClass()
    {
        String[] resolvedBundleNames = resolver.resolve(new ClassContext(MockSubClass.class));
        assertEquals(4, resolvedBundleNames.length);
        assertEquals("com/zutubi/i18n/mock/MockSubClass", resolvedBundleNames[0]);
        assertEquals("com/zutubi/i18n/mock/MockClass", resolvedBundleNames[1]);
        assertEquals("com/zutubi/i18n/mock/MockInterface", resolvedBundleNames[2]);
        assertEquals("java/lang/Object", resolvedBundleNames[3]);
    }
}
