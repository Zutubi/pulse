package com.zutubi.prototype.type;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.AbstractConfiguration;

import java.util.Hashtable;

/**
 *
 *
 */
public class CompositeTypeTest extends TypeTestCase
{
    private CompositeType basicType;
    private CompositeType typeA;

    protected void setUp() throws Exception
    {
        super.setUp();

        basicType = typeRegistry.register(BasicTypes.class);
        typeA = typeRegistry.register(ObjectTypeA.class);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testBasicTypesConversion() throws TypeException
    {
        BasicTypes instance = new BasicTypes();
        instance.setBooleanO(Boolean.TRUE);
        instance.setBooleanP(false);
        instance.setByteO((byte) 1);
        instance.setByteP((byte) 2);
        instance.setCharacterO('a');
        instance.setCharacterP('b');
        instance.setDoubleO(3.3);
        instance.setDoubleP(4.4);
        instance.setFloatO((float) 0.5);
        instance.setFloatP((float) 0.6);
        instance.setIntegerO(7);
        instance.setIntegerP(8);
        instance.setLongO(9L);
        instance.setLongP(10L);
        instance.setShortO((short) 11);
        instance.setShortP((short) 12);
        instance.setString("howdy");

        Record record = basicType.unstantiate(instance);
        SimpleInstantiator instantiator = new SimpleInstantiator(null);
        Object newInstance = instantiator.instantiate(basicType, record);
        assertTrue(newInstance instanceof BasicTypes);
        assertEquals(newInstance, instance);
    }

    public void testWithNestedComplexType() throws TypeException
    {
        ObjectTypeA instance = new ObjectTypeA();
        ObjectTypeB objectTypeB = new ObjectTypeB();
        objectTypeB.setA("b");
        instance.setA(objectTypeB);

        Record record = typeA.unstantiate(instance);
        SimpleInstantiator instantiator = new SimpleInstantiator(null);
        ObjectTypeA newInstance = (ObjectTypeA) instantiator.instantiate(typeA, record);

        assertNotNull(newInstance.getA());
        assertEquals("b", newInstance.getA().getA());
    }

    public void testCreateNewRecordInitialisedDefaultFields()
    {
        MutableRecord record = typeA.createNewRecord(true);
        assertNotNull(record);

        // field a contains an instance of typeB.
        assertNotNull(record.get("a"));

        // field b is null.
        assertNull(record.get("b"));

        // typeB has field a initialised to 'value'
        Record b = (Record) record.get("a");
        assertEquals("value", b.get("a"));
    }

    public void testToXmlRpcNull() throws TypeException
    {
        assertNull(typeA.toXmlRpc(null));
    }

    public void testToXmlRpc() throws TypeException
    {
        ObjectTypeA a = new ObjectTypeA();
        ObjectTypeB b = new ObjectTypeB();
        b.setA("string");
        a.setB(b);

        Record record = typeA.unstantiate(a);
        Object rpcForm = typeA.toXmlRpc(record);
        assertTrue(rpcForm instanceof Hashtable);

        Hashtable ht = (Hashtable) rpcForm;
        assertEquals(2, ht.size());
        assertNotNull(ht.get("a"));
        Hashtable member = (Hashtable) ht.get("b");
        assertEquals("string", member.get("a"));
    }

    public void testToXmlRpcNullNotIncluded() throws TypeException
    {
        ObjectTypeA a = new ObjectTypeA();
        Record record = typeA.unstantiate(a);
        Object rpcForm = typeA.toXmlRpc(record);
        assertTrue(rpcForm instanceof Hashtable);

        Hashtable ht = (Hashtable) rpcForm;
        assertEquals(1, ht.size());
        assertNotNull(ht.get("a"));
        assertNull(ht.get("b"));
    }

    @SymbolicName("typeA")
    public static class ObjectTypeA extends AbstractConfiguration
    {
        private ObjectTypeB a = new ObjectTypeB();

        private ObjectTypeB b = null;

        public ObjectTypeB getA()
        {
            return a;
        }

        public void setA(ObjectTypeB a)
        {
            this.a = a;
        }

        public ObjectTypeB getB()
        {
            return b;
        }

        public void setB(ObjectTypeB b)
        {
            this.b = b;
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ObjectTypeA that = (ObjectTypeA) o;

            return !(a != null ? !a.equals(that.a) : that.a != null);
        }

        public int hashCode()
        {
            return (a != null ? a.hashCode() : 0);
        }
    }

    @SymbolicName("typeB")
    public static class ObjectTypeB extends AbstractConfiguration
    {
        private String a = "value";

        public String getA()
        {
            return a;
        }

        public void setA(String a)
        {
            this.a = a;
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ObjectTypeB that = (ObjectTypeB) o;

            return !(a != null ? !a.equals(that.a) : that.a != null);
        }

        public int hashCode()
        {
            return (a != null ? a.hashCode() : 0);
        }
    }

    @SymbolicName("basicTypes")
    public static class BasicTypes extends AbstractConfiguration
    {
        private Boolean booleanO;
        private boolean booleanP;
        private Byte byteO;
        private byte byteP;
        private Character characterO;
        private char characterP;
        private Double doubleO;
        private double doubleP;
        private Float floatO;
        private float floatP;
        private Integer integerO;
        private int integerP;
        private Long longO;
        private long longP;
        private Short shortO;
        private short shortP;
        private String string;

        public Boolean getBooleanO()
        {
            return booleanO;
        }

        public void setBooleanO(Boolean booleanO)
        {
            this.booleanO = booleanO;
        }

        public boolean isBooleanP()
        {
            return booleanP;
        }

        public void setBooleanP(boolean booleanP)
        {
            this.booleanP = booleanP;
        }

        public Byte getByteO()
        {
            return byteO;
        }

        public void setByteO(Byte byteO)
        {
            this.byteO = byteO;
        }

        public byte getByteP()
        {
            return byteP;
        }

        public void setByteP(byte byteP)
        {
            this.byteP = byteP;
        }

        public Character getCharacterO()
        {
            return characterO;
        }

        public void setCharacterO(Character characterO)
        {
            this.characterO = characterO;
        }

        public char getCharacterP()
        {
            return characterP;
        }

        public void setCharacterP(char characterP)
        {
            this.characterP = characterP;
        }

        public Double getDoubleO()
        {
            return doubleO;
        }

        public void setDoubleO(Double doubleO)
        {
            this.doubleO = doubleO;
        }

        public double getDoubleP()
        {
            return doubleP;
        }

        public void setDoubleP(double doubleP)
        {
            this.doubleP = doubleP;
        }

        public Float getFloatO()
        {
            return floatO;
        }

        public void setFloatO(Float floatO)
        {
            this.floatO = floatO;
        }

        public float getFloatP()
        {
            return floatP;
        }

        public void setFloatP(float floatP)
        {
            this.floatP = floatP;
        }

        public Integer getIntegerO()
        {
            return integerO;
        }

        public void setIntegerO(Integer integerO)
        {
            this.integerO = integerO;
        }

        public int getIntegerP()
        {
            return integerP;
        }

        public void setIntegerP(int integerP)
        {
            this.integerP = integerP;
        }

        public Long getLongO()
        {
            return longO;
        }

        public void setLongO(Long longO)
        {
            this.longO = longO;
        }

        public long getLongP()
        {
            return longP;
        }

        public void setLongP(long longP)
        {
            this.longP = longP;
        }

        public Short getShortO()
        {
            return shortO;
        }

        public void setShortO(Short shortO)
        {
            this.shortO = shortO;
        }

        public short getShortP()
        {
            return shortP;
        }

        public void setShortP(short shortP)
        {
            this.shortP = shortP;
        }

        public String getString()
        {
            return string;
        }

        public void setString(String string)
        {
            this.string = string;
        }


        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            BasicTypes that = (BasicTypes) o;

            if (booleanP != that.booleanP)
            {
                return false;
            }
            if (byteP != that.byteP)
            {
                return false;
            }
            if (characterP != that.characterP)
            {
                return false;
            }
            if (Double.compare(that.doubleP, doubleP) != 0)
            {
                return false;
            }
            if (Float.compare(that.floatP, floatP) != 0)
            {
                return false;
            }
            if (integerP != that.integerP)
            {
                return false;
            }
            if (longP != that.longP)
            {
                return false;
            }
            if (shortP != that.shortP)
            {
                return false;
            }
            if (booleanO != null ? !booleanO.equals(that.booleanO) : that.booleanO != null)
            {
                return false;
            }
            if (byteO != null ? !byteO.equals(that.byteO) : that.byteO != null)
            {
                return false;
            }
            if (characterO != null ? !characterO.equals(that.characterO) : that.characterO != null)
            {
                return false;
            }
            if (doubleO != null ? !doubleO.equals(that.doubleO) : that.doubleO != null)
            {
                return false;
            }
            if (floatO != null ? !floatO.equals(that.floatO) : that.floatO != null)
            {
                return false;
            }
            if (integerO != null ? !integerO.equals(that.integerO) : that.integerO != null)
            {
                return false;
            }
            if (longO != null ? !longO.equals(that.longO) : that.longO != null)
            {
                return false;
            }
            if (shortO != null ? !shortO.equals(that.shortO) : that.shortO != null)
            {
                return false;
            }
            return !(string != null ? !string.equals(that.string) : that.string != null);
        }

        public int hashCode()
        {
            int result;
            long temp;
            result = (booleanO != null ? booleanO.hashCode() : 0);
            result = 31 * result + (booleanP ? 1 : 0);
            result = 31 * result + (byteO != null ? byteO.hashCode() : 0);
            result = 31 * result + (int) byteP;
            result = 31 * result + (characterO != null ? characterO.hashCode() : 0);
            result = 31 * result + (int) characterP;
            result = 31 * result + (doubleO != null ? doubleO.hashCode() : 0);
            temp = doubleP != +0.0d ? Double.doubleToLongBits(doubleP) : 0L;
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result + (floatO != null ? floatO.hashCode() : 0);
            result = 31 * result + floatP != +0.0f ? Float.floatToIntBits(floatP) : 0;
            result = 31 * result + (integerO != null ? integerO.hashCode() : 0);
            result = 31 * result + integerP;
            result = 31 * result + (longO != null ? longO.hashCode() : 0);
            result = 31 * result + (int) (longP ^ (longP >>> 32));
            result = 31 * result + (shortO != null ? shortO.hashCode() : 0);
            result = 31 * result + (int) shortP;
            result = 31 * result + (string != null ? string.hashCode() : 0);
            return result;
        }
    }

}
