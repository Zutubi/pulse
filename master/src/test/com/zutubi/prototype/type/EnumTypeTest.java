package com.zutubi.prototype.type;

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
        assertNull(type.toXmlRpc(null));
    }
    
    public void testToXmlRpc() throws TypeException
    {
        Object o = type.toXmlRpc("M2");
        assertTrue(o instanceof String);
        assertEquals("M2", o);
    }

    private enum TestEnum
    {
        M1,
        M2,
        M3,
    }
}
