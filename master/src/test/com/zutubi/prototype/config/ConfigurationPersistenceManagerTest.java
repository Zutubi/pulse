package com.zutubi.prototype.config;

import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MockRecordSerialiser;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.prototype.record.SymbolicName;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

/**
 */
public class ConfigurationPersistenceManagerTest extends TestCase
{
    private ConfigurationPersistenceManager manager = null;
    private TypeRegistry typeRegistry = null;
    private RecordManager recordManager = null;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeRegistry = new TypeRegistry();
        recordManager = new RecordManager();
        recordManager.setRecordSerialiser(new MockRecordSerialiser());
        manager = new ConfigurationPersistenceManager();
        manager.setTypeRegistry(typeRegistry);
        manager.setRecordManager(recordManager);
    }

    protected void tearDown() throws Exception
    {
        recordManager = null;
        typeRegistry = null;
        manager = null;

        super.tearDown();
    }

    public void testListingSimpleObject() throws TypeException
    {
        manager.register("simple", typeRegistry.register(SimpleObject.class));
        assertEquals(0, manager.getListing("simple").size());

        Type type = manager.getType("simple");
        assertEquals(SimpleObject.class, type.getClazz());
    }

/*
    public void testListingSimpleCollectionFromRecord() throws TypeException
    {
        manager.register("simpleCollection", typeRegistry.register(SimpleCollectionObject.class));
        assertEquals(0, manager.getListing("simpleCollection/simpleList").size());

        // now lets add some records.
        Record listData = new MutableRecord();
        listData.put("1", "a");
        listData.put("2", "b");

        MutableRecord scoRecord = new MutableRecord();
        scoRecord.put("simpleList", listData);
        recordManager.store("simpleCollection", scoRecord);

        assertEquals(2, manager.getListing("simpleCollection/simpleList").size());
    }
*/

    public void testListingNestedCollectionObject() throws TypeException
    {
        manager.register("nestedCollection", typeRegistry.register(NestedCollectionObject.class));
        assertEquals(1, manager.getListing("nestedCollection").size());
        assertEquals(0, manager.getListing("nestedCollection/nestedList").size());
    }

/*
    public void testLoadSimpleObject() throws TypeException
    {
        manager.register("simpleObject", typeRegistry.register("simpleObject", SimpleObject.class));

        Record simpleObject = new MutableRecord();
        simpleObject.setSymbolicName("simpleObject");
        simpleObject.put("strA", "a");
        simpleObject.put("strB", "b");
        recordManager.store("simpleObject", simpleObject);

        SimpleObject instance = (SimpleObject) manager.getInstance("simpleObject");
        assertEquals("a", instance.getStrA());
        assertEquals("b", instance.getStrB());
    }
*/

/*
    public void testStoreSimpleObject() throws TypeException
    {
        manager.register("simpleObject", typeRegistry.register("simpleObject", SimpleObject.class));

        SimpleObject instance = new SimpleObject();
        instance.setStrA("A");
        instance.setStrB("B");

        manager.setInstance("simpleObject", instance);

        Record record = recordManager.load("simpleObject");
        assertEquals("A", record.get("strA"));
        assertEquals("B", record.get("strB"));
    }
*/

/*
    public void testStoreInCollection() throws TypeException
    {
        typeRegistry.register("simpleObject", SimpleObject.class);
        manager.register("simpleCollection", typeRegistry.register(SimpleCollectionObject.class));
        assertEquals(0, manager.getListing("simpleCollection/simpleList").size());

        SimpleObject instance = new SimpleObject();
        instance.setStrA("string a");
        instance.setStrB("string b");

        manager.setInstance("simpleCollection/simpleList", instance);
        assertEquals(1, manager.getListing("simpleCollection/simpleList").size());

        manager.setInstance("simpleCollection/simpleList", instance);
        assertEquals(2, manager.getListing("simpleCollection/simpleList").size());

    }
*/

    @SymbolicName("Simple")
    public static class SimpleObject
    {
        private String strA;

        private String strB;

        public String getStrA()
        {
            return strA;
        }

        public void setStrA(String strA)
        {
            this.strA = strA;
        }

        public String getStrB()
        {
            return strB;
        }

        public void setStrB(String strB)
        {
            this.strB = strB;
        }
    }

    @SymbolicName("Nested")
    public static class NestedObject
    {
        private NestedObject nested;

        public NestedObject getNested()
        {
            return nested;
        }

        public void setNested(NestedObject nested)
        {
            this.nested = nested;
        }
    }

    @SymbolicName("Composite")
    public static class CompositeObject
    {
        private String strA;
        private String strB;
        private NestedObject nested;
        private CompositeObject composite;

        private List<String> list;
        private Map<String, String> map;

        public String getStrA()
        {
            return strA;
        }

        public void setStrA(String strA)
        {
            this.strA = strA;
        }

        public String getStrB()
        {
            return strB;
        }

        public void setStrB(String strB)
        {
            this.strB = strB;
        }

        public NestedObject getNested()
        {
            return nested;
        }

        public void setNested(NestedObject nested)
        {
            this.nested = nested;
        }

        public CompositeObject getComposite()
        {
            return composite;
        }

        public void setComposite(CompositeObject composite)
        {
            this.composite = composite;
        }

        public List<String> getList()
        {
            return list;
        }

        public void setList(List<String> list)
        {
            this.list = list;
        }

        public Map<String, String> getMap()
        {
            return map;
        }

        public void setMap(Map<String, String> map)
        {
            this.map = map;
        }
    }

    @SymbolicName("SimpleCollection")
    public static class SimpleCollectionObject
    {
        List<SimpleObject> simpleList;

        public List<SimpleObject> getSimpleList()
        {
            return simpleList;
        }

        public void setSimpleList(List<SimpleObject> simpleList)
        {
            this.simpleList = simpleList;
        }
    }

    @SymbolicName("NestedCollection")
    public static class NestedCollectionObject
    {
        List<NestedObject> nestedList;

        public List<NestedObject> getNestedList()
        {
            return nestedList;
        }

        public void setNestedList(List<NestedObject> nestedList)
        {
            this.nestedList = nestedList;
        }
    }
}
