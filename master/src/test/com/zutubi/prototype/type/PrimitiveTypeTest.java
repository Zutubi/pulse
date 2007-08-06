package com.zutubi.prototype.type;

/**
 */
public class PrimitiveTypeTest extends TypeTestCase
{
    public void testToXmlRpcBoolean() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Boolean.class);
        Object o = type.toXmlRpc("true");
        assertTrue(o instanceof Boolean);
        assertEquals(true, o);
    }

    public void testToXmlRpcByte() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Byte.class);
        Object o = type.toXmlRpc(Byte.toString((byte) 10));
        assertTrue(o instanceof Integer);
        assertEquals(10, o);
    }

    public void testToXmlRpcCharacter() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Character.class);
        Object o = type.toXmlRpc("a");
        assertTrue(o instanceof String);
        assertEquals("a", o);
    }

    public void testToXmlRpcDouble() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Double.class);
        Object o = type.toXmlRpc(Double.toString(1.24324));
        assertTrue(o instanceof Double);
        assertEquals(1.24324, o);
    }

    public void testToXmlRpcInt() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Integer.class);
        Object o = type.toXmlRpc(Integer.toString(987));
        assertTrue(o instanceof Integer);
        assertEquals(987, o);
    }

    public void testToXmlRpcFloat() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Float.class);
        Object o = type.toXmlRpc(Float.toString(1.2e-4f));
        assertTrue(o instanceof Double);
        assertTrue(Math.abs(1.2e-4 - (Double)o) < 1e-10);
    }

    public void testToXmlRpcLong() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Long.class);
        Object o = type.toXmlRpc(Long.toString(9634));
        assertTrue(o instanceof String);
        assertEquals("9634", o);
    }

    public void testToXmlRpcShort() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Short.class);
        Object o = type.toXmlRpc(Short.toString((short) 77));
        assertTrue(o instanceof Integer);
        assertEquals(77, o);
    }

    public void testToXmlRpcString() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(String.class);
        Object o = type.toXmlRpc("here is no why");
        assertTrue(o instanceof String);
        assertEquals("here is no why", o);
    }

    public void testToXmlRpcNull() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(String.class);
        assertNull(type.toXmlRpc(null));
    }

    public void testFromXmlRpcBoolean() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Boolean.class);
        Object o = type.fromXmlRpc(true);
        assertTrue(o instanceof String);
        assertEquals("true", o);
    }

    public void testFromXmlRpcByte() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Byte.class);
        Object o = type.fromXmlRpc(10);
        assertTrue(o instanceof String);
        assertEquals(Byte.toString((byte) 10), o);
    }

    public void testFromXmlRpcCharacter() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Character.class);
        Object o = type.fromXmlRpc("a");
        assertTrue(o instanceof String);
        assertEquals("a", o);
    }

    public void testFromXmlRpcDouble() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Double.class);
        Object o = type.fromXmlRpc(1.24324);
        assertTrue(o instanceof String);
        assertEquals(Double.toString(1.24324), o);
    }

    public void testFromXmlRpcInt() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Integer.class);
        Object o = type.fromXmlRpc(987);
        assertTrue(o instanceof String);
        assertEquals(Integer.toString(987), o);
    }

    public void testFromXmlRpcFloat() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Float.class);
        Object o = type.fromXmlRpc(1.2e-4);
        assertTrue(o instanceof String);
        assertEquals("1.2E-4", o);
    }

    public void testFromXmlRpcLong() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Long.class);
        Object o = type.fromXmlRpc("9634");
        assertTrue(o instanceof String);
        assertEquals(Long.toString(9634), o);
    }

    public void testFromXmlRpcShort() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Short.class);
        Object o = type.fromXmlRpc(77);
        assertTrue(o instanceof String);
        assertEquals(Short.toString((short) 77), o);
    }

    public void testfromXmlRpcString() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(String.class);
        Object o = type.fromXmlRpc("here is no why");
        assertTrue(o instanceof String);
        assertEquals("here is no why", o);
    }

    public void testFromXmlRpcNull() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(String.class);
        assertNull(type.toXmlRpc(null));
    }
    
}
