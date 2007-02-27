package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class ListTypeTest extends TypeTestCase
{
    private ListType listType;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeRegistry.register("mockA", MockA.class);
        typeRegistry.register("mockB", MockB.class);
        
        listType = new ListType();
        listType.setTypeRegistry(typeRegistry);
    }

    protected void tearDown() throws Exception
    {
        listType = null;

        super.tearDown();
    }

/*
    public void testSimpleList() throws TypeException
    {
        List<String> list = new LinkedList<String>();
        list.add("a");
        list.add("b");

        Record record = listType.unstantiate(list);

        List<Object> newList = listType.instantiate(record);

        assertEquals(2, newList.size());
        assertEquals("a", newList.get(0));
        assertEquals("b", newList.get(1));
    }

    public void testCompositeObjectList() throws TypeException
    {
        List<Object> list = new LinkedList<Object>();
        list.add(new MockA("valueA"));
        list.add(new MockB("valueB"));

        Record record = listType.unstantiate(list);

        List<Object> newList = listType.instantiate(record);
        assertEquals(2, newList.size());
        assertTrue(newList.get(0) instanceof MockA);
        assertTrue(newList.get(1) instanceof MockB);
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
    }
}
