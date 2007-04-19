package com.zutubi.i18n.context;

import junit.framework.TestCase;

/**
 * <class-comment/>
 */
public class ClassContextResolverTest extends TestCase
{
    private ClassContextResolver resolver;

    protected void setUp() throws Exception
    {
        super.setUp();

        resolver = new ClassContextResolver();
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

    public void testClassContext()
    {
        String[] resolvedBundleNames = resolver.resolve(new ClassContext(ClassContext.class));
        assertEquals(3, resolvedBundleNames.length);
        assertEquals("com/zutubi/i18n/context/ClassContext", resolvedBundleNames[0]);
        assertEquals("com/zutubi/i18n/context/Context", resolvedBundleNames[1]);
        assertEquals("java/lang/Object", resolvedBundleNames[2]);
    }
}
