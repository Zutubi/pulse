package com.zutubi.tove.type;

/**
 */
public class EnumTypeTest extends TypeTestCase
{
    private EnumType type;


    protected void setUp() throws Exception
    {
        type = new EnumType(TestEnum.class);
    }

    public void testToXmlRpcNull() throws TypeException
    {
        assertNull(type.toXmlRpc(null, null));
    }
    
    public void testToXmlRpc() throws TypeException
    {
        Object o = type.toXmlRpc(null, "M2");
        assertTrue(o instanceof String);
        assertEquals("M2", o);
    }

    public void testFromXmlRpc() throws TypeException
    {
        Object o = type.fromXmlRpc(null, "M2", true);
        assertTrue(o instanceof String);
        assertEquals("M2", o);
    }

    public void testFromXmlRpcWrongType()
    {
        try
        {
            type.fromXmlRpc(null, new Integer(2), true);
            fail();
        }
        catch (TypeException e)
        {
            assertEquals("Expecting 'java.lang.String', found 'java.lang.Integer'", e.getMessage());
        }
    }

    private enum TestEnum
    {
        M1,
        M2,
        M3,
    }
}
