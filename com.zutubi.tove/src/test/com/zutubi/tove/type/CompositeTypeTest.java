package com.zutubi.tove.type;

import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.lang.annotation.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class CompositeTypeTest extends TypeTestCase
{
    private CompositeType basicType;
    private CompositeType typeA;
    private CompositeType typeB;
    private CompositeType baseType;
    private CompositeType extensionType;
    private CompositeType interfaceType;
    private CompositeType grandparentType;
    private CompositeType parentType;
    private CompositeType childType;
    private CompositeType refType;

    protected void setUp() throws Exception
    {
        super.setUp();

        basicType = typeRegistry.register(BasicTypes.class);
        basicType.setTypeRegistry(typeRegistry);
        typeA = typeRegistry.register(ObjectTypeA.class);
        typeB = typeRegistry.getType(ObjectTypeB.class);
        typeRegistry.register(ObjectTypeBExtension.class);

        baseType = typeRegistry.register(BaseConfiguration.class);
        extensionType = typeRegistry.register(ExtensionConfiguration.class);
        interfaceType = typeRegistry.register(ConfigInterface.class);
        grandparentType = typeRegistry.register(GrandparentConfig.class);
        parentType = typeRegistry.register(ParentConfig.class);
        childType = typeRegistry.register(ChildConfig.class);
        refType = typeRegistry.register(RefConfig.class);
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

        Record record = basicType.unstantiate(instance, null);
        SimpleInstantiator instantiator = new SimpleInstantiator(null, null, configurationTemplateManager);
        Object newInstance = instantiator.instantiate(basicType, record);
        assertTrue(newInstance instanceof BasicTypes);
        assertEquals(newInstance, instance);
    }

    public void testBasicTypesInstantiateNull() throws TypeException
    {
        MutableRecord record = basicType.createNewRecord(false);
        for(TypeProperty property: basicType.getProperties())
        {
            if(property.getName().endsWith("P"))
            {
                record.put(property.getName(), "");
            }
        }

        SimpleInstantiator instantiator = new SimpleInstantiator(null, null, configurationTemplateManager);
        BasicTypes instance = (BasicTypes) basicType.instantiate(record, instantiator);
        basicType.initialise(instance, record, instantiator);
        assertEquals(false, instance.isBooleanP());
        assertEquals(Byte.MIN_VALUE, instance.getByteP());
        assertEquals(Character.MIN_VALUE, instance.getCharacterP());
        assertEquals(Double.MIN_VALUE, instance.getDoubleP());
        assertEquals(Float.MIN_VALUE, instance.getFloatP());
        assertEquals(Integer.MIN_VALUE, instance.getIntegerP());
        assertEquals(Long.MIN_VALUE, instance.getLongP());
        assertEquals(Short.MIN_VALUE, instance.getShortP());
    }

    public void testWithNestedComplexType() throws TypeException
    {
        ObjectTypeA instance = new ObjectTypeA();
        ObjectTypeB objectTypeB = new ObjectTypeB();
        objectTypeB.setA("b");
        instance.setA(objectTypeB);

        Record record = typeA.unstantiate(instance, null);
        SimpleInstantiator instantiator = new SimpleInstantiator(null, null, configurationTemplateManager);
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
        assertNull(typeA.toXmlRpc(null, null));
    }

    public void testToXmlRpc() throws TypeException
    {
        ObjectTypeA a = new ObjectTypeA();
        ObjectTypeB b = new ObjectTypeB();
        b.setA("string");
        a.setB(b);

        Record record = typeA.unstantiate(a, null);
        Object rpcForm = typeA.toXmlRpc(null, record);
        assertTrue(rpcForm instanceof Hashtable);

        Hashtable ht = (Hashtable) rpcForm;
        assertEquals(3, ht.size());
        assertEquals("typeA", ht.get("meta.symbolicName"));
        assertNotNull(ht.get("a"));
        Hashtable member = (Hashtable) ht.get("b");
        assertEquals("typeB", member.get("meta.symbolicName"));
        assertEquals("string", member.get("a"));
    }

    public void testToXmlRpcNullNotIncluded() throws TypeException
    {
        ObjectTypeA a = new ObjectTypeA();
        Record record = typeA.unstantiate(a, null);
        Object rpcForm = typeA.toXmlRpc(null, record);
        assertTrue(rpcForm instanceof Hashtable);

        Hashtable ht = (Hashtable) rpcForm;
        assertEquals(2, ht.size());
        assertEquals("typeA", ht.get("meta.symbolicName"));
        assertNotNull(ht.get("a"));
        assertNull(ht.get("b"));
    }

    public void testToXmlRpcNullReference() throws TypeException
    {
        RefConfig config = new RefConfig();
        Record record = refType.unstantiate(config, null);
        Hashtable rpcForm = refType.toXmlRpc(null, record);
        assertFalse(rpcForm.containsKey("ref"));
    }

    public void testFromXmlRpc() throws TypeException
    {
        Hashtable inner = new Hashtable();
        inner.put("meta.symbolicName", "typeB");
        inner.put("a", "avalue");

        Hashtable outer = new Hashtable();
        outer.put("meta.symbolicName", "typeA");
        outer.put("a", inner);

        Object o = typeA.fromXmlRpc(null, outer, true);
        assertTrue(o instanceof Record);
        Record outerRecord = (Record) o;
        assertEquals(1, outerRecord.size());
        assertEquals("typeA", outerRecord.getSymbolicName());

        o = outerRecord.get("a");
        assertNotNull(o);
        assertTrue(o instanceof Record);
        Record innerRecord = (Record) o;
        assertEquals("typeB", innerRecord.getSymbolicName());
        assertEquals(1, innerRecord.size());
        assertEquals("avalue", innerRecord.get("a"));
    }

    public void testFromXmlRpcNoSymbolicName() throws TypeException
    {
        try
        {
            Hashtable rpcForm = new Hashtable();
            rpcForm.put("a", "avalue");
            typeB.fromXmlRpc(null, rpcForm, true);
            fail();
        }
        catch (TypeException e)
        {
            assertEquals("No symbolic name found in XML-RPC struct", e.getMessage());
        }
    }

    public void testFromXmlRpcInvalidSymbolicName() throws TypeException
    {
        try
        {
            Hashtable rpcForm = new Hashtable();
            rpcForm.put("meta.symbolicName", "grr");
            rpcForm.put("a", "avalue");
            typeB.fromXmlRpc(null, rpcForm, true);
            fail();
        }
        catch (TypeException e)
        {
            assertEquals("XML-RPC struct has unrecognised symbolic name 'grr'", e.getMessage());
        }
    }

    public void testFromXmlRpcWrongType()
    {
        try
        {
            typeB.fromXmlRpc(null, "string", true);
            fail();
        }
        catch (TypeException e)
        {
            assertEquals("Expecting 'java.util.Hashtable', found 'java.lang.String'", e.getMessage());
        }
    }

    public void testFromXmlRpcWrongPropertyType() throws TypeException
    {
        try
        {
            Hashtable rpcForm = new Hashtable();
            rpcForm.put("meta.symbolicName", "typeB");
            rpcForm.put("a", new Vector());
            typeB.fromXmlRpc(null, rpcForm, true);
            fail();
        }
        catch (TypeException e)
        {
            assertEquals("Converting property 'a' of type 'typeB': Expecting 'java.lang.String', found 'java.util.Vector'", e.getMessage());
        }
    }

    public void testFromXmlRpcUnrecognisedProperty()
    {
        try
        {
            Hashtable rpcForm = new Hashtable();
            rpcForm.put("meta.symbolicName", "typeB");
            rpcForm.put("unknown", "ignored value");

            typeB.fromXmlRpc(null, rpcForm, true);
            fail();
        }
        catch (TypeException e)
        {
            assertEquals("Unrecognised property 'unknown' for type 'typeB'", e.getMessage());
        }
    }

    public void testIsValid()
    {
        assertTrue(typeA.isValid(new ObjectTypeA()));
    }

    public void testIsValidDirectlyInvalid()
    {
        ObjectTypeA a = new ObjectTypeA();
        a.addInstanceError("error");
        assertFalse(typeA.isValid(a));
    }

    public void testIsValidNestedInvalid()
    {
        ObjectTypeA a = new ObjectTypeA();
        a.getA().addInstanceError("error");
        assertFalse(typeA.isValid(a));
    }

    public void testIsValidExtensionType()
    {
        ObjectTypeBExtension extension = new ObjectTypeBExtension();
        ObjectTypeC c = new ObjectTypeC();
        c.addInstanceError("error");
        extension.setC(c);
        assertFalse(typeB.isValid(extension));
    }

    public void testIsValidExtensionProperty()
    {
        ObjectTypeC c = new ObjectTypeC();
        c.addInstanceError("error");
        ObjectTypeBExtension extension = new ObjectTypeBExtension();
        extension.setC(c);
        ObjectTypeA a = new ObjectTypeA();
        a.setB(extension);
        assertFalse(typeA.isValid(a));
    }

    public void testHasAnnotationDirect()
    {
        assertTrue(extensionType.hasAnnotation(DirectAnnotation.class, true));
        assertTrue(extensionType.hasAnnotation(DirectAnnotation.class, false));
    }

    public void testHasAnnotationInherited()
    {
        assertTrue(extensionType.hasAnnotation(InheritedAnnotation.class, true));
        assertFalse(extensionType.hasAnnotation(InheritedAnnotation.class, false));
    }

    public void testHasAnnotationOverridden()
    {
        assertTrue(extensionType.hasAnnotation(OverriddenAnnotation.class, true));
        assertTrue(extensionType.hasAnnotation(OverriddenAnnotation.class, false));
    }

    public void testHasAnnotationIndirect()
    {
        assertTrue(childType.hasAnnotation(GrandparentAnnotation.class, true));
        assertFalse(childType.hasAnnotation(GrandparentAnnotation.class, false));
    }

    public void testHasAnnotationFromInterface()
    {
        assertTrue(childType.hasAnnotation(InterfaceAnnotation.class, true));
        assertFalse(childType.hasAnnotation(InterfaceAnnotation.class, false));
    }
    
    public void testGetAnnotationDirect()
    {
        assertNotNull(extensionType.getAnnotation(DirectAnnotation.class, true));
        assertNotNull(extensionType.getAnnotation(DirectAnnotation.class, false));
    }

    public void testGetAnnotationInherited()
    {
        assertNotNull(extensionType.getAnnotation(InheritedAnnotation.class, true));
        assertNull(extensionType.getAnnotation(InheritedAnnotation.class, false));
    }

    public void testGetAnnotationOverridden()
    {
        assertEquals("extension", extensionType.getAnnotation(OverriddenAnnotation.class, true).value());
        assertEquals("extension", extensionType.getAnnotation(OverriddenAnnotation.class, true).value());
    }

    public void testGetAnnotationIndirect()
    {
        assertNotNull(childType.getAnnotation(GrandparentAnnotation.class, true));
        assertNull(childType.getAnnotation(GrandparentAnnotation.class, false));
    }

    public void testGetAnnotationFromInterface()
    {
        assertNotNull(childType.getAnnotation(InterfaceAnnotation.class, true));
        assertNull(childType.getAnnotation(InterfaceAnnotation.class, false));
    }

    public void testGetAnnotations()
    {
        List<Annotation> annotations = baseType.getAnnotations(false);
        assertEquals(3, annotations.size());
        assertAnnotation(annotations, SymbolicName.class);
        assertAnnotation(annotations, InheritedAnnotation.class);
        assertAnnotation(annotations, OverriddenAnnotation.class);

        annotations = baseType.getAnnotations(true);
        assertEquals(3, annotations.size());
        assertAnnotation(annotations, SymbolicName.class);
        assertAnnotation(annotations, InheritedAnnotation.class);
        assertAnnotation(annotations, OverriddenAnnotation.class);
    }

    public void testGetAnnotationsExtension()
    {
        List<Annotation> annotations = extensionType.getAnnotations(false);
        assertEquals(3, annotations.size());
        assertAnnotation(annotations, SymbolicName.class);
        assertAnnotation(annotations, DirectAnnotation.class);
        assertAnnotation(annotations, OverriddenAnnotation.class);

        annotations = extensionType.getAnnotations(true);
        assertEquals(6, annotations.size());
        assertAnnotationCount(2, annotations, SymbolicName.class);
        assertAnnotation(annotations, DirectAnnotation.class);
        assertAnnotation(annotations, InheritedAnnotation.class);
        assertAnnotationCount(2, annotations, OverriddenAnnotation.class);
    }

    public void testGetAnnotationsIndirectAndInterface()
    {
        List<Annotation> annotations = childType.getAnnotations(false);
        assertEquals(2, annotations.size());
        assertAnnotation(annotations, SymbolicName.class);
        assertAnnotation(annotations, ChildAnnotation.class);

        annotations = childType.getAnnotations(true);
        assertEquals(8, annotations.size());
        assertAnnotationCount(4, annotations, SymbolicName.class);
        assertAnnotation(annotations, GrandparentAnnotation.class);
        assertAnnotation(annotations, ParentAnnotation.class);
        assertAnnotation(annotations, InterfaceAnnotation.class);
        assertAnnotation(annotations, ChildAnnotation.class);
    }

    public void testGetSpecificAnnotationsDirect()
    {
        List<Annotation> annotations = extensionType.getAnnotations(DirectAnnotation.class, false);
        assertEquals(1, annotations.size());
        annotations = extensionType.getAnnotations(DirectAnnotation.class, true);
        assertEquals(1, annotations.size());
    }

    public void testGetSpecificAnnotationsInherited()
    {
        List<Annotation> annotations = extensionType.getAnnotations(InheritedAnnotation.class, false);
        assertEquals(0, annotations.size());
        annotations = extensionType.getAnnotations(InheritedAnnotation.class, true);
        assertEquals(1, annotations.size());
    }

    public void testGetSpecificAnnotationsOverridden()
    {
        List<Annotation> annotations = extensionType.getAnnotations(OverriddenAnnotation.class, false);
        assertEquals(1, annotations.size());
        annotations = extensionType.getAnnotations(OverriddenAnnotation.class, true);
        assertEquals(2, annotations.size());
    }

    public void testGetSpecificAnnotationsIndirect()
    {
        List<Annotation> annotations = childType.getAnnotations(GrandparentAnnotation.class, false);
        assertEquals(0, annotations.size());
        annotations = childType.getAnnotations(GrandparentAnnotation.class, true);
        assertEquals(1, annotations.size());
    }

    public void testGetSpecificAnnotationsFromInterface()
    {
        List<Annotation> annotations = childType.getAnnotations(InterfaceAnnotation.class, false);
        assertEquals(0, annotations.size());
        annotations = childType.getAnnotations(InterfaceAnnotation.class, true);
        assertEquals(1, annotations.size());
    }

    private void assertAnnotation(List<Annotation> annotations, final Class clazz)
    {
        assertTrue(CollectionUtils.contains(annotations, new Predicate<Annotation>()
        {
            public boolean satisfied(Annotation annotation)
            {
                return annotation.annotationType() == clazz;
            }
        }));
    }

    private void assertAnnotationCount(int count, List<Annotation> annotations, final Class clazz)
    {
        assertEquals(count, CollectionUtils.filter(annotations, new Predicate<Annotation>()
        {
            public boolean satisfied(Annotation annotation)
            {
                return annotation.annotationType() == clazz;
            }
        }).size());
    }

    public void testSupertypesRegistered()
    {
        List<CompositeType> superTypes = childType.getSuperTypes();
        assertEquals(2, superTypes.size());
        assertTrue(superTypes.contains(interfaceType));
        assertTrue(superTypes.contains(parentType));

        superTypes = interfaceType.getSuperTypes();
        assertEquals(0, superTypes.size());

        superTypes = parentType.getSuperTypes();
        assertEquals(1, superTypes.size());
        assertTrue(superTypes.contains(grandparentType));

        superTypes = grandparentType.getSuperTypes();
        assertEquals(0, superTypes.size());
    }
    
    public void testExtensionsRegistered()
    {
        List<CompositeType> extensions = grandparentType.getExtensions();
        assertEquals(1, extensions.size());
        assertTrue(extensions.contains(childType));

        extensions = parentType.getExtensions();
        assertEquals(1, extensions.size());
        assertTrue(extensions.contains(childType));

        extensions = interfaceType.getExtensions();
        assertEquals(1, extensions.size());
        assertTrue(extensions.contains(childType));

        extensions = childType.getExtensions();
        assertEquals(0, extensions.size());
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

    @SymbolicName("typeBExtension")
    public static class ObjectTypeBExtension extends ObjectTypeB
    {
        private ObjectTypeC c;

        public ObjectTypeC getC()
        {
            return c;
        }

        public void setC(ObjectTypeC c)
        {
            this.c = c;
        }
    }

    @SymbolicName("typeC")
    public static class ObjectTypeC extends AbstractConfiguration
    {

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

    @InheritedAnnotation
    @OverriddenAnnotation("base")
    @SymbolicName("basetype")
    public static abstract class BaseConfiguration extends AbstractConfiguration
    {
    }

    @DirectAnnotation
    @OverriddenAnnotation("extension")
    @SymbolicName("extensiontype")
    public static class ExtensionConfiguration extends BaseConfiguration
    {
    }

    @InterfaceAnnotation
    @SymbolicName("configinterface")
    public static interface ConfigInterface extends Configuration
    {
    }

    @GrandparentAnnotation
    @SymbolicName("grandparentconfig")
    public static abstract class GrandparentConfig extends AbstractConfiguration
    {
    }

    @ParentAnnotation
    @SymbolicName("parentconfig")
    public static abstract class ParentConfig extends GrandparentConfig
    {
    }

    @ChildAnnotation
    @SymbolicName("childconfig")
    public static class ChildConfig extends ParentConfig implements ConfigInterface
    {
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface DirectAnnotation
    {
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface InheritedAnnotation
    {
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface OverriddenAnnotation
    {
        String value();
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface InterfaceAnnotation
    {
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface GrandparentAnnotation
    {
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ParentAnnotation
    {
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ChildAnnotation
    {
    }

    @SymbolicName("ref")
    public static class RefConfig extends AbstractConfiguration
    {
        @Reference
        private EeConfig ref;

        public EeConfig getRef()
        {
            return ref;
        }

        public void setRef(EeConfig ref)
        {
            this.ref = ref;
        }
    }

    @SymbolicName("ee")
    public static class EeConfig extends AbstractNamedConfiguration
    {
    }
}
