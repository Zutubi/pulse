package com.zutubi.prototype.type;

/**
 *
 *
 */
public class MapTypeTest extends  TypeTestCase
{
    private MapType mapType;

    protected void setUp() throws Exception
    {
        super.setUp();

        mapType = new MapType(configurationPersistenceManager);
        mapType.setTypeRegistry(typeRegistry);

        typeRegistry.register("mockA", MockA.class);
        typeRegistry.register("mockB", MockB.class);
    }

    protected void tearDown() throws Exception
    {
        mapType = null;

        super.tearDown();
    }

    public void test()
    {
        // noop.
    }

/*
    public void testPrimitiveMap() throws TypeException
    {
        Map<String, Integer> instance = new HashMap<String, Integer>();
        instance.put("key", 1);
        instance.put("a", 2);

        Record record = mapType.unstantiate(instance);
        Map newInstance = mapType.instantiate(record);

        assertEquals(2, newInstance.size());
        assertEquals(1, newInstance.get("key"));
        assertEquals(2, newInstance.get("a"));
    }

    public void testCompositeObjectMap() throws TypeException
    {
        Map<String, Object> instance = new HashMap<String, Object>();
        instance.put("keyA", new MockA("valueA"));
        instance.put("keyB", new MockB("valueB"));

        Record record = mapType.unstantiate(instance);
        Map newInstance = mapType.instantiate(record);

        assertEquals(2, newInstance.size());
        assertEquals(instance.get("keyA"), newInstance.get("keyA"));
        assertEquals(instance.get("keyB"), newInstance.get("keyB"));
    }
*/

    public static class MockA
    {
        private String a;

        public MockA()
        {
        }

        public MockA(String a)
        {
            this.a = a;
        }

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

            MockA mockA = (MockA) o;

            if (a != null ? !a.equals(mockA.a) : mockA.a != null) return false;

            return true;
        }

        public int hashCode()
        {
            return (a != null ? a.hashCode() : 0);
        }
    }

    public static class MockB
    {
        private String b;

        public MockB()
        {
        }

        public MockB(String b)
        {
            this.b = b;
        }

        public String getB()
        {
            return b;
        }

        public void setB(String b)
        {
            this.b = b;
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MockB mockB = (MockB) o;

            if (b != null ? !b.equals(mockB.b) : mockB.b != null) return false;

            return true;
        }

        public int hashCode()
        {
            return (b != null ? b.hashCode() : 0);
        }
    }
}
