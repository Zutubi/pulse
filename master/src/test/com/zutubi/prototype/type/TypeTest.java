package com.zutubi.prototype.type;

import junit.framework.TestCase;
import com.zutubi.prototype.type.record.Record;

import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 *
 *
 */
public class TypeTest extends TestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testPrimitiveStringInstantiation() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(String.class);
        String instance = (String) type.instantiate("value");
        assertEquals("value", instance);
    }

    public void testPrimitiveIntegerInstantiation() throws TypeException
    {
        PrimitiveType type = new PrimitiveType(Integer.class);
        Integer instance = (Integer) type.instantiate("1");
        assertEquals(Integer.valueOf(1), instance);
    }

    public void testCompositeInstantiation() throws TypeException
    {
        Record record = new Record();
        record.put("name", "myName");

        TypeRegistry registry = new TypeRegistry();
        CompositeType type = registry.register("symbolicName", MockA.class);
        MockA mock = (MockA) type.instantiate(record);
        assertEquals("myName", mock.getName());
    }

    public void testCompositeUnstantiation() throws TypeException
    {
        MockA mock = new MockA();
        mock.setName("myName");

        TypeRegistry registry = new TypeRegistry();
        CompositeType type = registry.register("symbolicName", MockA.class);

        Record record = type.unstantiate(mock);
        assertEquals("myName", record.get("name"));
    }

    public void testListInstantiation() throws TypeException
    {
        Record record = new Record();
        Record list = new Record();
        list.put("0", "a");
        list.put("1", "b");
        record.put("list", list);

        TypeRegistry registry = new TypeRegistry();
        CompositeType type = registry.register("symbolicName", MockB.class);

        MockB mock = (MockB) type.instantiate(record);
        assertNotNull(mock.getList());
        assertEquals(2, mock.getList().size());
    }

    public void testListUnstantiation() throws TypeException
    {
        MockB mock = new MockB();
        mock.setList(Arrays.asList("a", "b"));

        TypeRegistry registry = new TypeRegistry();
        CompositeType type = registry.register("symbolicName", MockB.class);

        Record record = type.unstantiate(mock);
        assertEquals(1, record.size());

        Record list = (Record) record.get("list");
        assertEquals(2, list.size());
        assertEquals("a", list.get("0"));
        assertEquals("b", list.get("1"));
    }

    public void testNestedCompositeInstantiation() throws TypeException
    {
        Record record = new Record();
        record.put("name", "a");
        Record nested = new Record();
        nested.put("name", "b");
        record.put("nested", nested);

        TypeRegistry registry = new TypeRegistry();
        CompositeType type = registry.register("symbolicName", MockC.class);

        MockC mock = (MockC) type.instantiate(record);
        assertEquals("a", mock.getName());
        assertEquals("b", mock.getNested().getName());
    }

    public void testNestedCompositeUnstantiation() throws TypeException
    {
        MockC mockA = new MockC();
        mockA.setName("a");
        MockC mockB = new MockC();
        mockB.setName("b");
        mockA.setNested(mockB);

        TypeRegistry registry = new TypeRegistry();
        CompositeType type = registry.register("symbolicName", MockC.class);

        Record record = type.unstantiate(mockA);
        assertEquals("a", record.get("name"));

        Record recordB = (Record) record.get("nested");
        assertEquals("b", recordB.get("name"));
    }

    public void testMapInstantiation() throws TypeException
    {
        Record map = new Record();
        map.put("A", "B");
        map.put("C", "D");
        Record record = new Record();
        record.put("map", map);

        TypeRegistry registry = new TypeRegistry();
        CompositeType type = registry.register("symbolicName", MockD.class);

        MockD mock = (MockD) type.instantiate(record);
        assertNotNull(mock.getMap());
        assertEquals("B", mock.getMap().get("A"));
        assertEquals("D", mock.getMap().get("C"));
    }


    public static class MockA
    {
        private String name;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }
    }

    public static class MockB
    {
        private List<String> list;

        public List<String> getList()
        {
            return list;
        }

        public void setList(List<String> list)
        {
            this.list = list;
        }
    }

    public static class MockC
    {
        private String name;

        private MockC nested;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public MockC getNested()
        {
            return nested;
        }

        public void setNested(MockC nested)
        {
            this.nested = nested;
        }
    }

    public static class MockD
    {
        private Map<String, String> map;

        public Map<String, String> getMap()
        {
            return map;
        }

        public void setMap(Map<String, String> map)
        {
            this.map = map;
        }
    }
}
