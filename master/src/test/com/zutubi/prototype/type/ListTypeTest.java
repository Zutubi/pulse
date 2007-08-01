package com.zutubi.prototype.type;

import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.AbstractConfiguration;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 */
@SuppressWarnings({ "unchecked" })
public class ListTypeTest extends TypeTestCase
{
    private ListType listType;
    private ListType simpleListType;
    private CompositeType mockAType;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockAType = typeRegistry.register(MockA.class);

        listType = new ListType(recordManager);
        listType.setTypeRegistry(typeRegistry);
        listType.setCollectionType(mockAType);

        PrimitiveType stringType = new PrimitiveType(String.class);
        simpleListType = new ListType(recordManager);
        simpleListType.setTypeRegistry(typeRegistry);
        simpleListType.setCollectionType(stringType);
    }

    protected void tearDown() throws Exception
    {
        listType = null;
        mockAType = null;
        
        super.tearDown();
    }

    public void testCompositeObjectList() throws TypeException
    {
        List<Object> list = new LinkedList<Object>();
        list.add(new MockA("valueA"));
        list.add(new MockA("valueB"));

        Record record = (Record) listType.unstantiate(list);

        SimpleInstantiator instantiator = new SimpleInstantiator(null);
        List<Object> newList = (List<Object>) instantiator.instantiate(listType, record);
        assertEquals(2, newList.size());
        assertTrue(newList.get(0) instanceof MockA);
    }

    public void testInsertionPath() throws TypeException
    {
        long lastHandle = recordManager.allocateHandle();
        MutableRecord record = mockAType.unstantiate(new MockA("valueA"));
        assertEquals("coll/" + Long.toString(lastHandle + 1), listType.getInsertionPath("coll", record));
    }

    public void testSavePath() throws TypeException
    {
        MutableRecord record = mockAType.unstantiate(new MockA("valueA"));
        assertEquals("any/path", listType.getSavePath("any/path", record));
    }

    public void testToXmlRpcNull() throws TypeException
    {
        assertNull(listType.toXmlRpc(null));
    }

    public void testToXmlRpcEmptyRecord() throws TypeException
    {
        MutableRecord record = listType.createNewRecord(true);
        Object o = listType.toXmlRpc(record);
        assertTrue(o instanceof Vector);
        Vector v = (Vector) o;
        assertEquals(0, v.size());
    }

    public void testToXmlRpcRecord() throws TypeException
    {
        List<MockA> l = new LinkedList<MockA>();
        l.add(new MockA("one"));
        l.add(new MockA("two"));
        Record r = (Record) listType.unstantiate(l);

        Object rpcForm = listType.toXmlRpc(r);
        assertTrue(rpcForm instanceof Vector);
        Vector rpcVector = (Vector) rpcForm;
        assertEquals(2, rpcVector.size());
        Object element = rpcVector.get(0);
        assertTrue(element instanceof Hashtable);
        assertEquals("one", ((Hashtable)element).get("a"));
        element = rpcVector.get(1);
        assertTrue(element instanceof Hashtable);
        assertEquals("two", ((Hashtable)element).get("a"));
    }

    public void testToXmlRpcEmptyArray() throws TypeException
    {
        Object o = simpleListType.toXmlRpc(new String[0]);
        assertTrue(o instanceof Vector);
        Vector v = (Vector) o;
        assertEquals(0, v.size());
    }

    public void testToXmlRpcArray() throws TypeException
    {
        Object o = simpleListType.toXmlRpc(new String[] {"one", "two"});
        assertTrue(o instanceof Vector);
        Vector v = (Vector) o;
        assertEquals(2, v.size());
        assertEquals("one", v.get(0));
        assertEquals("two", v.get(1));
    }

    @SymbolicName("mockA")
    public static class MockA extends AbstractConfiguration
    {
        @ID
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
}
