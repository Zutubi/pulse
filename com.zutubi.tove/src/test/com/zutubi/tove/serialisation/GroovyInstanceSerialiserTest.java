package com.zutubi.tove.serialisation;

import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.util.junit.ZutubiTestCase;

public class GroovyInstanceSerialiserTest extends ZutubiTestCase
{
    private static final String EXTENSION = "groovy";

    public void testSimple()
    {
        GroovyInstanceSerialiser serialiser = new GroovyInstanceSerialiser();
        SimpleConfig c = serialiser.deserialise(getInput(EXTENSION), SimpleConfig.class);
        assertEquals("test string", c.getS());
    }

    public static class SimpleConfig extends AbstractConfiguration
    {
        private String s;

        public SimpleConfig(String s)
        {
            this.s = s;
        }

        public String getS()
        {
            return s;
        }

        public void setS(String s)
        {
            this.s = s;
        }
    }
}
