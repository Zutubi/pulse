package com.zutubi.prototype.type;

import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;

/**
 *
 *
 */
public class TypeConversionSupportTest extends TestCase
{
    private TypeRegistry typeRegistry;
    private TypeConversionSupport conversionSupport;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeRegistry = new TypeRegistry();
        conversionSupport = new TypeConversionSupport();
        conversionSupport.setTypeRegistry(typeRegistry);
    }

    protected void tearDown() throws Exception
    {
        typeRegistry = null;

        super.tearDown();
    }

    public void testSimpleTypeConversionsFromMap() throws TypeException
    {
        PrimitiveTypes types = new PrimitiveTypes();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("b", "false");
        data.put("s", "some string");
        data.put("i", "123");

        conversionSupport.applyMapTo(data, types);

        assertEquals(false, types.isB());
        assertEquals("some string", types.getS());
        assertEquals(Integer.valueOf(123), types.getI());
    }

    public void testSimpleTypeConversionsToMap() throws TypeException
    {
        PrimitiveTypes types = new PrimitiveTypes();
        types.setB(true);
        types.setI(321);
        types.setS("some other string");

        Map<String, Object> map = new HashMap<String, Object>();

        conversionSupport.applyToMap(types, map);

        assertEquals("true", map.get("b"));
        assertEquals("321", map.get("i"));
        assertEquals("some other string", map.get("s"));
    }

    public static class PrimitiveTypes
    {
        private boolean b;
        private String s;
        private Integer i;

        public boolean isB()
        {
            return b;
        }

        public void setB(boolean b)
        {
            this.b = b;
        }

        public String getS()
        {
            return s;
        }

        public void setS(String s)
        {
            this.s = s;
        }

        public Integer getI()
        {
            return i;
        }

        public void setI(Integer i)
        {
            this.i = i;
        }
    }
}
