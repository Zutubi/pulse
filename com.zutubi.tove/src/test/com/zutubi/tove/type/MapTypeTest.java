package com.zutubi.tove.type;

import com.zutubi.tove.annotations.ID;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.ConfigurationMap;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class MapTypeTest extends TypeTestCase
{
    private MapType mapType;
    private MapType orderedMapType;

    private CompositeType mockAType;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockAType = typeRegistry.register(ConfigA.class);
        mapType = new MapType(mockAType, typeRegistry);
        orderedMapType = new MapType(mockAType, typeRegistry);
        orderedMapType.setOrdered(true);
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
        instance.put("keyA", new ConfigA("valueA"));
        instance.put("keyB", new ConfigA("valueB"));

        Record record = mapType.unstantiate(instance, null);
        SimpleInstantiator instantiator = new SimpleInstantiator(null, null, configurationTemplateManager);
        Map newInstance = (Map) instantiator.instantiate(mapType, record);

        assertEquals(2, newInstance.size());
        assertEquals(instance.get("keyA"), newInstance.get("keyA"));
        assertEquals(instance.get("keyB"), newInstance.get("keyB"));
    }

    public void testGetItemKeyNoPath() throws TypeException
    {
        Record record = mockAType.unstantiate(new ConfigA("valueA"), null);
        assertEquals("valueA", mapType.getItemKey(null, record));
    }

    public void testGetItemKeyPath() throws TypeException
    {
        Record record = mockAType.unstantiate(new ConfigA("valueA"), null);
        assertEquals("valueA", mapType.getItemKey("coll/oldkey", record));
    }

    public void testToXmlRpcNull() throws TypeException
    {
        assertNull(mapType.toXmlRpc(null, null));
    }

    public void testToXmlRpcEmptyRecord() throws TypeException
    {
        Record record = mapType.createNewRecord(true);
        Object o = mapType.toXmlRpc(null, record);
        assertTrue(o instanceof Hashtable);
        assertEquals(0, ((Hashtable)o).size());
    }

    public void testToXmlRpc() throws TypeException
    {
        Map<String, ConfigA> m = new HashMap<String, ConfigA>();
        m.put("key1", new ConfigA("1"));
        m.put("key2", new ConfigA("2"));
        Record record = mapType.unstantiate(m, null);
        Object o = mapType.toXmlRpc(null, record);
        assertTrue(o instanceof Hashtable);
        @SuppressWarnings("unchecked")
        Hashtable<String, Hashtable> rpcForm = (Hashtable<String, Hashtable>) o;
        assertEquals(2, rpcForm.size());
        assertEquals("1", rpcForm.get("key1").get("a"));
        assertEquals("2", rpcForm.get("key2").get("a"));
    }

    public void testFromXmlRpc() throws TypeException
    {
        Hashtable<String, Object> element = new Hashtable<String, Object>();
        element.put("meta.symbolicName", "configA");
        element.put("a", "avalue");

        Hashtable<String, Object> rpcForm = new Hashtable<String, Object>();
        rpcForm.put("avalue", element);

        Object o = mapType.fromXmlRpc(null, rpcForm, true);
        assertTrue(o instanceof Record);
        Record record = (Record) o;
        assertEquals(1, record.size());
        o = record.get("avalue");
        assertNotNull(o);
        assertTrue(o instanceof Record);
        record = (Record) o;
        assertEquals("configA", record.getSymbolicName());
        assertEquals("avalue", record.get("a"));
    }

    public void testFromXmlRpcEmptyMap() throws TypeException
    {
        Hashtable rpcForm = new Hashtable();

        Object o = mapType.fromXmlRpc(null, rpcForm, true);
        assertTrue(o instanceof Record);
        Record record = (Record) o;
        assertEquals(0, record.size());
    }

    public void testFromXmlRpcInvalidType() throws TypeException
    {
        try
        {
            mapType.fromXmlRpc(null, "string", true);
            fail();
        }
        catch (TypeException e)
        {
            assertEquals("Expecting 'java.util.Hashtable', found 'java.lang.String'", e.getMessage());
        }
    }

    public void testFromXmlRpcInvalidElementType() throws TypeException
    {
        try
        {
            Hashtable<String, Object> rpcForm = new Hashtable<String, Object>();
            rpcForm.put("a", "avalue");
            mapType.fromXmlRpc(null, rpcForm, true);
            fail();
        }
        catch (TypeException e)
        {
            assertEquals("Converting map element 'a': Expecting 'java.util.Map', found 'java.lang.String'", e.getMessage());
        }
    }

    public void testFromXmlRpcInvalidKeyType() throws TypeException
    {
        try
        {
            Hashtable<Integer, Object> rpcForm = new Hashtable<Integer, Object>();
            rpcForm.put(1, "avalue");
            mapType.fromXmlRpc(null, rpcForm, true);
            fail();
        }
        catch (TypeException e)
        {
            assertEquals("Map element has invalid key type: Expecting 'java.lang.String', found 'java.lang.Integer'", e.getMessage());
        }
    }

    public void testIsValid()
    {
        ConfigurationMap<ConfigA> map = new ConfigurationMap<ConfigA>();
        map.put("a", new ConfigA("a"));
        assertTrue(mapType.isValid(map));
    }

    public void testIsValidDirectlyInvalid()
    {
        ConfigurationMap<ConfigA> map = new ConfigurationMap<ConfigA>();
        map.addInstanceError("error");
        map.put("a", new ConfigA("a"));
        assertFalse(mapType.isValid(map));
    }

    public void testIsValidElementInvalid()
    {
        ConfigA element = new ConfigA("a");
        element.addInstanceError("error");
        ConfigurationMap<ConfigA> map = new ConfigurationMap<ConfigA>();
        map.put("a", element);
        assertFalse(mapType.isValid(map));
    }

    public void testIsValidElementIndirectlyInvalid()
    {
        ConfigB nested = new ConfigB();
        nested.addInstanceError("error");
        ConfigA element = new ConfigA("a");
        element.setConfigB(nested);
        ConfigurationMap<ConfigA> map = new ConfigurationMap<ConfigA>();
        map.put("a", element);
        assertFalse(mapType.isValid(map));
    }

    public void testOrderPreserverOnUnstantiate() throws TypeException
    {
        ConfigurationMap<ConfigA> aMap = new ConfigurationMap<ConfigA>();
        aMap.put("foo", new ConfigA("foo"));
        aMap.put("bar", new ConfigA("bar"));
        aMap.put("baz", new ConfigA("baz"));

        MutableRecord record = orderedMapType.unstantiate(aMap, null);
        assertEquals(Arrays.asList("foo", "bar", "baz"), orderedMapType.getOrder(record));

        // Trying a second order gurantees wwe don't get lucky by matching
        // whatever the unordered default is.
        aMap.clear();
        aMap.put("baz", new ConfigA("baz"));
        aMap.put("foo", new ConfigA("foo"));
        aMap.put("bar", new ConfigA("bar"));

        record = orderedMapType.unstantiate(aMap, null);
        assertEquals(Arrays.asList("baz", "foo", "bar"), orderedMapType.getOrder(record));
    }

    @SymbolicName("configA")
    public static class ConfigA extends AbstractConfiguration
    {
        @ID
        private String a;
        private ConfigB configB;

        public ConfigA()
        {
        }

        public ConfigA(String a)
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

            ConfigA configA = (ConfigA) o;

            return !(a != null ? !a.equals(configA.a) : configA.a != null);

        }

        public int hashCode()
        {
            return (a != null ? a.hashCode() : 0);
        }

        public ConfigB getConfigB()
        {
            return configB;
        }

        public void setConfigB(ConfigB configB)
        {
            this.configB = configB;
        }
    }

    @SymbolicName("configB")
    public static class ConfigB extends AbstractConfiguration
    {
    }
}
