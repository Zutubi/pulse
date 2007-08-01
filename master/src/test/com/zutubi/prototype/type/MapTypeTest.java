package com.zutubi.prototype.type;

import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.AbstractConfiguration;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 *
 *
 */
public class MapTypeTest extends TypeTestCase
{
    private MapType mapType;

    private CompositeType mockAType;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockAType = typeRegistry.register(MockA.class);

        mapType = new MapType();
        mapType.setTypeRegistry(typeRegistry);
        mapType.setCollectionType(typeRegistry.getType(MockA.class));
    }

    protected void tearDown() throws Exception
    {
        mapType = null;
        mockAType = null;

        super.tearDown();
    }

    public void testCompositeObjectMap() throws TypeException
    {
        Map<String, Object> instance = new HashMap<String, Object>();
        instance.put("keyA", new MockA("valueA"));
        instance.put("keyB", new MockA("valueB"));

        Record record = (Record) mapType.unstantiate(instance);
        SimpleInstantiator instantiator = new SimpleInstantiator(null);
        Map newInstance = (Map) instantiator.instantiate(mapType, record);

        assertEquals(2, newInstance.size());
        assertEquals(instance.get("keyA"), newInstance.get("keyA"));
        assertEquals(instance.get("keyB"), newInstance.get("keyB"));
    }

    public void testInsertionPath() throws TypeException
    {
        Record record = mockAType.unstantiate(new MockA("valueA"));
        assertEquals("coll/valueA", mapType.getInsertionPath("coll", record));
    }

    public void testSavePath() throws TypeException
    {
        Record record = mockAType.unstantiate(new MockA("valueA"));

        assertEquals("coll/valueA", mapType.getInsertionPath("coll", record));
        assertEquals("coll/valueA", mapType.getSavePath("coll/valueA", record));
    }

    public void testToXmlRpcNull() throws TypeException
    {
        assertNull(mapType.toXmlRpc(null));
    }

    public void testToXmlRpcEmptyRecord() throws TypeException
    {
        Record record = mapType.createNewRecord(true);
        Object o = mapType.toXmlRpc(record);
        assertTrue(o instanceof Hashtable);
        assertEquals(0, ((Hashtable)o).size());
    }

    public void testToXmlRpc() throws TypeException
    {
        Map<String, MockA> m = new HashMap<String, MockA>();
        m.put("key1", new MockA("1"));
        m.put("key2", new MockA("2"));
        Record record = (Record) mapType.unstantiate(m);
        Object o = mapType.toXmlRpc(record);
        assertTrue(o instanceof Hashtable);
        Hashtable<String, Hashtable> rpcForm = ((Hashtable) o);
        assertEquals(2, rpcForm.size());
        assertEquals("1", rpcForm.get("key1").get("a"));
        assertEquals("2", rpcForm.get("key2").get("a"));
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

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MockA mockA = (MockA) o;

            return !(a != null ? !a.equals(mockA.a) : mockA.a != null);

        }

        public int hashCode()
        {
            return (a != null ? a.hashCode() : 0);
        }
    }
}
