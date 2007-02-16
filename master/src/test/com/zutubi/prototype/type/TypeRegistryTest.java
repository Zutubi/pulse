package com.zutubi.prototype.type;

import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

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
        Type type = typeRegistry.register("name", Mock.class);

        assertTrue(type instanceof CompositeType);

        CompositeType compositeType = (CompositeType) type;
        assertTrue(compositeType.hasProperty("name"));
        assertTrue(compositeType.hasProperty("names"));
        assertTrue(compositeType.hasProperty("mock"));
        assertTrue(compositeType.hasProperty("mocks"));
    }

    public static class Mock
    {
        private String name;

        private List<String> names;

        private Mock mock;

        private Map<String, Mock> mocks;

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
    }

}
