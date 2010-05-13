package com.zutubi.tove.type;

import com.zutubi.tove.annotations.ID;
import com.zutubi.tove.annotations.ReadOnly;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.validation.annotations.Required;

import java.util.List;
import java.util.Map;

public class TypeRegistryTest extends ZutubiTestCase
{
    private TypeRegistry typeRegistry = new TypeRegistry();

    public void testSimpleObject() throws TypeException
    {
        CompositeType type = typeRegistry.register(FakeConfiguration.class);

        assertTrue(type.hasProperty("name"));
        assertTrue(type.hasProperty("names"));
        assertTrue(type.hasProperty("fake"));
        assertTrue(type.hasProperty("fakes"));
        assertTrue(type.hasProperty("anotherFake"));
    }

    public void testSimpleInterfaceHolder() throws TypeException
    {
        CompositeType type = typeRegistry.register(SimpleInterfaceHolder.class);

        assertTrue(type.hasProperty("simpleInterface"));
        assertEquals(1, type.getProperties().size());
    }

    public void testAnnotations() throws TypeException
    {
        // Note that the registry also gathers meta-annotations.
        CompositeType type = typeRegistry.register(FakeConfiguration.class);
        assertEquals(1, type.getAnnotations(false).size());
        TypeProperty propertyType = type.getProperty("name");
        assertEquals(3, propertyType.getAnnotations().size());
        propertyType = type.getProperty("fake");
        assertEquals(0, propertyType.getAnnotations().size());
        propertyType = type.getProperty("anotherFake");
        assertEquals(2, propertyType.getAnnotations().size());
    }

    public void testPropertyTypes() throws TypeException
    {
        CompositeType type = typeRegistry.register(FakeConfiguration.class);

        List<String> mapProperties = type.getPropertyNames(MapType.class);
        assertEquals(1, mapProperties.size());
        assertEquals("fakes", mapProperties.get(0));

        List<String> listProperties = type.getPropertyNames(ListType.class);
        assertEquals(1, listProperties.size());
        assertEquals("names", listProperties.get(0));

        List<String> simpleProperties = type.getPropertyNames(PrimitiveType.class);
        assertEquals(1, simpleProperties.size());
        assertEquals("name", simpleProperties.get(0));

        List<String> nestedProperties = type.getPropertyNames(CompositeType.class);
        assertEquals(2, nestedProperties.size());
        assertTrue(nestedProperties.contains("fake"));
        assertTrue(nestedProperties.contains("anotherFake"));
    }

    public void testRegistration() throws TypeException
    {
        Type type = typeRegistry.register(SimpleObject.class);

        assertEquals(type, typeRegistry.getType(SimpleObject.class));

        // registering the same class a second time will return the original class.
        assertEquals(type, typeRegistry.register(SimpleObject.class));
    }

    public void testRegistrationRequiresSymbolicName()
    {
        try
        {
            typeRegistry.register(InvalidObject.class);
            fail();
        }
        catch (TypeException e)
        {
        }
    }

    public void testReadOnlyFields() throws TypeException
    {
        CompositeType c = typeRegistry.register(ReadOnlyFieldA.class);
        TypeProperty a = c.getProperty("a");
        assertTrue(a.isReadable());
        assertFalse(a.isWritable());
    }

    public void testReadOnlyFieldViaAnnotation() throws TypeException
    {
        CompositeType c = typeRegistry.register(ReadOnlyFieldB.class);
        TypeProperty b = c.getProperty("b");
        assertTrue(b.isReadable());
        assertFalse(b.isWritable());
    }

    public void testCanSetNameFieldOnNamedConfigurationToReadOnly() throws TypeException
    {
        CompositeType c = typeRegistry.register(ReadOnlyFieldName.class);
        TypeProperty name = c.getProperty("name");
        assertTrue(name.isReadable());
        assertFalse(name.isWritable());
    }

    public void testTransientFieldsNotIncluded() throws TypeException
    {
        CompositeType c = typeRegistry.register(TransientFieldA.class);
        assertNull(c.getProperty("a"));
    }

    public void testExtensionOfInterface() throws TypeException
    {
        CompositeType iType = typeRegistry.register(IExtend.class);
        CompositeType eType = typeRegistry.register(MeImplement.class);
        
        assertEquals(1, iType.getExtensions().size());
        assertEquals(eType, iType.getExtensions().get(0));
    }

    @SymbolicName("iExtend")
    public static interface IExtend extends Configuration
    {
    }

    @SymbolicName("meImplement")
    public static class MeImplement extends AbstractConfiguration implements IExtend
    {
    }
    
    public void testExtensionOfAbstractClass() throws TypeException
    {
        CompositeType baseType = typeRegistry.register(AbstractBase.class);
        CompositeType extensionType = typeRegistry.register(AbstractExtension.class);

        assertEquals(1, baseType.getExtensions().size());
        assertEquals(extensionType, baseType.getExtensions().get(0));
    }

    @SymbolicName("abstractBase")
    public static abstract class AbstractBase extends AbstractConfiguration
    {
    }

    @SymbolicName("abstractExtension")
    public static class AbstractExtension extends AbstractBase
    {
    }

    public void testExtensionOfConcreteClass() throws TypeException
    {
        CompositeType baseType = typeRegistry.register(ConcreteBase.class);
        typeRegistry.register(ConcreteExtension.class);

        assertEquals(0, baseType.getExtensions().size());
    }

    @SymbolicName("concreteBase")
    public static class ConcreteBase extends AbstractConfiguration
    {
    }

    @SymbolicName("concreteExtension")
    public static class ConcreteExtension extends ConcreteBase
    {
    }

    public void testMultipleAndIndirectExtensions() throws TypeException
    {
        CompositeType theBaseType = typeRegistry.register(TheBase.class);
        CompositeType directConcreteExtensionType = typeRegistry.register(DirectConcreteExtension.class);
        CompositeType directAbstractExtensionType = typeRegistry.register(DirectAbstractExtension.class);
        CompositeType concreteExtensionOfConcreteExtensionType = typeRegistry.register(ConcreteExtensionOfConcreteExtension.class);
        CompositeType concreteExtensionOfAbstractExtensionType = typeRegistry.register(ConcreteExtensionOfAbstractExtension.class);

        List<CompositeType> extensions = theBaseType.getExtensions();
        assertEquals(3, extensions.size());
        assertTrue(extensions.contains(directConcreteExtensionType));
        assertTrue(extensions.contains(concreteExtensionOfConcreteExtensionType));
        assertTrue(extensions.contains(concreteExtensionOfAbstractExtensionType));
        assertFalse(extensions.contains(directAbstractExtensionType));

        extensions = directConcreteExtensionType.getExtensions();
        assertEquals(0, extensions.size());
        
        extensions = directAbstractExtensionType.getExtensions();
        assertEquals(1, extensions.size());
        assertTrue(extensions.contains(concreteExtensionOfAbstractExtensionType));
    }

    @SymbolicName("theBase")
    public static abstract class TheBase extends AbstractConfiguration
    {
    }

    @SymbolicName("directConcreteExtension")
    public static class DirectConcreteExtension extends TheBase
    {
    }

    @SymbolicName("directAbstractExtension")
    public static abstract class DirectAbstractExtension extends TheBase
    {
    }

    @SymbolicName("concreteExtensionOfConcreteExtension")
    public static class ConcreteExtensionOfConcreteExtension extends DirectConcreteExtension
    {
    }

    @SymbolicName("concreteExtensionOfAbstractExtension")
    public static class ConcreteExtensionOfAbstractExtension extends DirectAbstractExtension
    {
    }

    /**
     * Read only field A defined by the absence of a setter.
     */
    @SymbolicName("readOnlyFieldA")
    public static class ReadOnlyFieldA extends AbstractConfiguration
    {
        private String a;

        public ReadOnlyFieldA()
        {
            
        }

        public ReadOnlyFieldA(String a)
        {
            this.a = a;
        }

        public String getA()
        {
            return a;
        }
    }

    /**
     * Read only field b defined by the presence of the @ReadOnly annotation.
     */
    @SymbolicName("readOnlyFieldB")
    public static class ReadOnlyFieldB extends AbstractConfiguration
    {
        @ReadOnly
        private String b;

        public String getB()
        {
            return b;
        }

        public void setB(String b)
        {
            this.b = b;
        }
    }

    @SymbolicName("transientFieldA")
    public static class TransientFieldA extends AbstractConfiguration
    {
        @Transient
        private String a;

        public String getA()
        {
            return a;
        }

        public void setA(String a)
        {
            this.a = a;
        }
    }

    @SymbolicName("readOnlyFieldName")
    public static class ReadOnlyFieldName extends AbstractNamedConfiguration
    {
        @ReadOnly
        public String getName()
        {
            return super.getName();
        }

        public void setName(String name)
        {
            super.setName(name);
        }
    }

    @SymbolicName("fakeName")
    public static class FakeConfiguration extends AbstractConfiguration
    {
        @ID
        private String name;

        private List<String> names;

        private FakeConfiguration fake;

        private FakeConfiguration anotherFake;

        private Map<String, FakeConfiguration> fakes;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public List<String> getNames()
        {
            return names;
        }

        public void setNames(List<String> names)
        {
            this.names = names;
        }

        public Map<String, FakeConfiguration> getFakes()
        {
            return fakes;
        }

        public void setFakes(Map<String, FakeConfiguration> fakes)
        {
            this.fakes = fakes;
        }

        public FakeConfiguration getFake()
        {
            return fake;
        }

        public void setFake(FakeConfiguration fakeConfiguration)
        {
            this.fake = fakeConfiguration;
        }

        @Required()
        public FakeConfiguration getAnotherFake()
        {
            return anotherFake;
        }

        public void setAnotherFake(FakeConfiguration anotherFakeConfiguration)
        {
            this.anotherFake = anotherFakeConfiguration;
        }
    }

    @SymbolicName("simpleObject")
    public static class SimpleObject extends AbstractConfiguration
    {
        private String b;

        public String getB()
        {
            return b;
        }

        public void setB(String b)
        {
            this.b = b;
        }
    }

    /**
     * No symbolic name makes this invalid.
     */
    public static class InvalidObject extends AbstractConfiguration
    {
        private String c;

        public String getC()
        {
            return c;
        }

        public void setC(String c)
        {
            this.c = c;
        }
    }

    @SymbolicName("simpleInterface")
    public static interface SimpleInterface extends Configuration
    {
        String getA();
        void setA(String str);
    }

    @SymbolicName("simpleInterfaceHolder")
    public static class SimpleInterfaceHolder extends AbstractConfiguration
    {
        private SimpleInterface simpleInterface;

        public SimpleInterface getSimpleInterface()
        {
            return simpleInterface;
        }

        public void setSimpleInterface(SimpleInterface simpleInterface)
        {
            this.simpleInterface = simpleInterface;
        }
    }

}
