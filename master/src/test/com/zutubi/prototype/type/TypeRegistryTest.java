package com.zutubi.prototype.type;

import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

import com.zutubi.validation.annotations.Required;
import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 *
 *
 */
public class TypeRegistryTest extends TestCase
{
    private TypeRegistry typeRegistry;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeRegistry = new TypeRegistry();
    }

    protected void tearDown() throws Exception
    {
        typeRegistry = null;

        super.tearDown();
    }

    public void testSimpleObject() throws TypeException
    {
        CompositeType type = typeRegistry.register("mockName", Mock.class);

        assertTrue(type.hasProperty("name"));
        assertTrue(type.hasProperty("names"));
        assertTrue(type.hasProperty("mock"));
        assertTrue(type.hasProperty("mocks"));
        assertTrue(type.hasProperty("anotherMock"));
    }

    public void testSimpleInterfaceHolder() throws TypeException
    {
        CompositeType type = typeRegistry.register("mockName", SimpleInterfaceHolder.class);

        assertTrue(type.hasProperty("simpleInterface"));
        assertEquals(1, type.getProperties().size());
    }

    public void testAnnotations() throws TypeException
    {
        CompositeType type = typeRegistry.register("mockName", Mock.class);
        assertEquals(1, type.getAnnotations().size());
        TypeProperty propertyType = type.getProperty("name");
        assertEquals(1, propertyType.getAnnotations().size());
        propertyType = type.getProperty("mock");
        assertEquals(0, propertyType.getAnnotations().size());
        propertyType = type.getProperty("anotherMock");
        assertEquals(1, propertyType.getAnnotations().size());
    }

    public void testPropertyTypes() throws TypeException
    {
        CompositeType type = typeRegistry.register("mockName", Mock.class);

        List<String> mapProperties = type.getPropertyNames(MapType.class);
        assertEquals(1, mapProperties.size());
        assertEquals("mocks", mapProperties.get(0));

        List<String> listProperties = type.getPropertyNames(ListType.class);
        assertEquals(1, listProperties.size());
        assertEquals("names", listProperties.get(0));

        List<String> simpleProperties = type.getPropertyNames(PrimitiveType.class);
        assertEquals(1, simpleProperties.size());
        assertEquals("name", simpleProperties.get(0));

        List<String> nestedProperties = type.getPropertyNames(CompositeType.class);
        assertEquals(2, nestedProperties.size());
        assertTrue(nestedProperties.contains("mock"));
        assertTrue(nestedProperties.contains("anotherMock"));
    }

    public void testRegistration() throws TypeException
    {
        Type type = typeRegistry.register(SimpleObject.class);

        assertEquals(type, typeRegistry.getType(SimpleObject.class));

        // registering the same class a second time will return the original class.
        assertEquals(type, typeRegistry.register(SimpleObject.class));

        // ensure that there is no prior registration to the someName key.
        assertNull(typeRegistry.getType("someName"));
        typeRegistry.register("someName", SimpleObject.class);
        assertEquals(type, typeRegistry.getType("someName"));

        // can have multiple symbolic names to the same type.
        typeRegistry.register("someOtherName", SimpleObject.class);
        assertEquals(typeRegistry.getType("someName"), typeRegistry.getType("someOtherName"));

        // can not reassign existing type definition.
        try
        {
            typeRegistry.register("someName", Map.class);
            fail();
        }
        catch (TypeException e)
        {
        }
    }

    @SymbolicName("mockName")
    public static class Mock
    {
        private String name;

        private List<String> names;

        private Mock mock;

        private Mock anotherMock;

        private Map<String, Mock> mocks;

        @Required()
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

        public Map<String, Mock> getMocks()
        {
            return mocks;
        }

        public void setMocks(Map<String, Mock> mocks)
        {
            this.mocks = mocks;
        }

        public Mock getMock()
        {
            return mock;
        }

        public void setMock(Mock mock)
        {
            this.mock = mock;
        }

        @Required()
        public Mock getAnotherMock()
        {
            return anotherMock;
        }

        public void setAnotherMock(Mock anotherMock)
        {
            this.anotherMock = anotherMock;
        }
    }

    public static class SimpleObject
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

    public static interface SimpleInterface
    {
        String getA();
        void setA(String str);
    }

    public static class SimpleInterfaceHolder
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
