package com.zutubi.i18n.bundle;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ExpanderTest extends TestCase
{
    private Expander expander;

    protected void setUp() throws Exception
    {
        super.setUp();

        expander = new Expander();
    }

    protected void tearDown() throws Exception
    {
        expander = null;

        super.tearDown();
    }

    public void testExpandWithVariant()
    {
        List<String> names = expander.expand("base", new Locale("de", "de", "ch"), ".x");
        assertEquals(4, names.size());
        assertEquals(Arrays.asList("base.x", "base_de.x", "base_de_DE.x", "base_de_DE_ch.x"), names);
    }

    private void assertEquals(List<String> is, List<String> should)
    {
        assertEquals(is.size(),should.size());
        for (String toCheck : should)
        {
            assertTrue(is.contains(toCheck));
        }
    }
}
