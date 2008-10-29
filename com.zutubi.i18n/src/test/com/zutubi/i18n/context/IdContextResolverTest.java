package com.zutubi.i18n.context;

import junit.framework.TestCase;

public class IdContextResolverTest extends TestCase
{
    private IdContextResolver resolver;

    protected void setUp() throws Exception
    {
        super.setUp();

        resolver = new IdContextResolver();
    }

    protected void tearDown() throws Exception
    {
        resolver = null;

        super.tearDown();
    }

    public void testKnownId()
    {
        IdContext ctx = new IdContext("id");
        resolver.addBundle(ctx, "bundleName");
        String[] resolvedNames = resolver.resolve(ctx);
        assertEquals(1, resolvedNames.length);
        assertEquals("bundleName", resolvedNames[0]);
    }

    public void testUnknownId()
    {
        IdContext ctx = new IdContext("id");
        resolver.addBundle(ctx, "bundleName");
        String[] resolvedNames = resolver.resolve(new IdContext("unknownId"));
        assertEquals(0, resolvedNames.length);
    }

}
