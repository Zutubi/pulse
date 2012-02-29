package com.zutubi.tove.type;

/**
 */
public class PrimitiveTypeTest extends TypeTestCase
{
    public void testToXmlRpcBoolean() throws TypeException
    {
        toXmlRpcHelper(Boolean.class, "true", Boolean.class, true);
    }

    public void testToXmlRpcBooleanPrimitive() throws TypeException
    {
        toXmlRpcHelper(boolean.class, "true", Boolean.class, true);
    }

    public void testToXmlRpcByte() throws TypeException
    {
        toXmlRpcHelper(Byte.class, Byte.toString((byte) 10), Integer.class, 10);
    }

    public void testToXmlRpcBytePrimitive() throws TypeException
    {
        toXmlRpcHelper(byte.class, Byte.toString((byte) 10), Integer.class, 10);
    }

    public void testToXmlRpcCharacter() throws TypeException
    {
        toXmlRpcHelper(Character.class, "a", String.class, "a");
    }

    public void testToXmlRpcCharacterPrimitive() throws TypeException
    {
        toXmlRpcHelper(char.class, "a", String.class, "a");
    }

    public void testToXmlRpcDouble() throws TypeException
    {
        toXmlRpcHelper(Double.class, Double.toString(1.24324), Double.class, 1.24324);
    }

    public void testToXmlRpcDoublePrimitive() throws TypeException
    {
        toXmlRpcHelper(double.class, Double.toString(1.24324), Double.class, 1.24324);
    }

    public void testToXmlRpcInt() throws TypeException
    {
        toXmlRpcHelper(Integer.class, Integer.toString(987), Integer.class, 987);
    }

    public void testToXmlRpcIntPrimitive() throws TypeException
    {
        toXmlRpcHelper(int.class, Integer.toString(987), Integer.class, 987);
    }

    public void testToXmlRpcFloat() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Float.class);
        Object o = type.toXmlRpc(null, Float.toString(1.2e-4f));
        assertTrue(o instanceof Double);
        assertTrue(Math.abs(1.2e-4 - (Double)o) < 1e-10);
    }

    public void testToXmlRpcFloatPrimitive() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(float.class);
        Object o = type.toXmlRpc(null, Float.toString(1.2e-4f));
        assertTrue(o instanceof Double);
        assertTrue(Math.abs(1.2e-4 - (Double)o) < 1e-10);
    }

    public void testToXmlRpcLong() throws TypeException
    {
        toXmlRpcHelper(Long.class, Long.toString(9634), String.class, "9634");
    }

    public void testToXmlRpcLongPrimitive() throws TypeException
    {
        toXmlRpcHelper(long.class, Long.toString(9634), String.class, "9634");
    }

    public void testToXmlRpcShort() throws TypeException
    {
        toXmlRpcHelper(Short.class, Short.toString((short) 77), Integer.class, 77);
    }

    public void testToXmlRpcShortPrimitive() throws TypeException
    {
        toXmlRpcHelper(short.class, Short.toString((short) 77), Integer.class, 77);
    }

    public void testToXmlRpcString() throws TypeException
    {
        toXmlRpcHelper(String.class, "here is no why", String.class, "here is no why");
    }

    private void toXmlRpcHelper(Class fromClazz, String from, Class toClazz, Object to) throws TypeException
    {
        PrimitiveType type = new PrimitiveType(fromClazz);
        Object o = type.toXmlRpc(null, from);
        assertTrue(toClazz.isInstance(o));
        assertEquals(to, o);
    }

    public void testToXmlRpcNull() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(String.class);
        assertNull(type.toXmlRpc(null, null));
    }

    public void testFromXmlRpcBoolean() throws TypeException
    {
        fromXmlRpcHelper(Boolean.class, true, "true");
    }

    public void testFromXmlRpcBooleanPrimitive() throws TypeException
    {
        fromXmlRpcHelper(boolean.class, true, "true");
    }

    public void testFromXmlRpcByte() throws TypeException
    {
        fromXmlRpcHelper(Byte.class, 10, Byte.toString((byte) 10));
    }

    public void testFromXmlRpcBytePrimitive() throws TypeException
    {
        fromXmlRpcHelper(byte.class, 10, Byte.toString((byte) 10));
    }

    public void testFromXmlRpcCharacter() throws TypeException
    {
        fromXmlRpcHelper(Character.class, "a", "a");
    }

    public void testFromXmlRpcCharacterPrimitive() throws TypeException
    {
        fromXmlRpcHelper(char.class, "a", "a");
    }

    public void testFromXmlRpcDouble() throws TypeException
    {
        fromXmlRpcHelper(Double.class, 1.24324, Double.toString(1.24324));
    }

    public void testFromXmlRpcDoublePrimitive() throws TypeException
    {
        fromXmlRpcHelper(double.class, 1.24324, Double.toString(1.24324));
    }

    public void testFromXmlRpcInt() throws TypeException
    {
        fromXmlRpcHelper(Integer.class, 987, Integer.toString(987));
    }

    public void testFromXmlRpcIntPrimitive() throws TypeException
    {
        fromXmlRpcHelper(int.class, 987, Integer.toString(987));
    }

    public void testFromXmlRpcFloat() throws TypeException
    {
        fromXmlRpcHelper(Float.class, 1.2e-4, "1.2E-4");
    }

    public void testFromXmlRpcFloatPrimitive() throws TypeException
    {
        fromXmlRpcHelper(float.class, 1.2e-4, "1.2E-4");
    }

    public void testFromXmlRpcLong() throws TypeException
    {
        fromXmlRpcHelper(Long.class, "9634", Long.toString(9634));
    }

    public void testFromXmlRpcLongPrimitive() throws TypeException
    {
        fromXmlRpcHelper(long.class, "9634", Long.toString(9634));
    }

    public void testFromXmlRpcShort() throws TypeException
    {
        fromXmlRpcHelper(Short.class, 77, Short.toString((short) 77));
    }

    public void testFromXmlRpcShortPrimitive() throws TypeException
    {
        fromXmlRpcHelper(short.class, 77, Short.toString((short) 77));
    }

    public void testfromXmlRpcString() throws TypeException
    {
        fromXmlRpcHelper(String.class, "here is no why", "here is no why");
    }

    private void fromXmlRpcHelper(Class fromClass, Object from, String to) throws TypeException
    {
        PrimitiveType type = new PrimitiveType(fromClass);
        Object o = type.fromXmlRpc(null, from, true);
        assertTrue(o instanceof String);
        assertEquals(to, o);
    }

    public void testFromXmlRpcNull() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(String.class);
        assertNull(type.toXmlRpc(null, null));
    }
    
}
