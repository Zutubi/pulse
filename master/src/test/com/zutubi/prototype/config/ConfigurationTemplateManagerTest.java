package com.zutubi.prototype.config;

import com.zutubi.config.annotations.Reference;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.config.events.PostInsertEvent;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.TemplatedMapType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.core.config.NamedConfiguration;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.validation.annotations.Required;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class ConfigurationTemplateManagerTest extends AbstractConfigurationSystemTestCase
{
    private CompositeType typeA;
    private CompositeType typeB;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeA = typeRegistry.register(MockA.class);
        typeB = typeRegistry.getType(MockB.class);
        MapType mapA = new MapType();
        mapA.setTypeRegistry(typeRegistry);
        mapA.setCollectionType(typeA);

        MapType templatedMap = new TemplatedMapType();
        templatedMap.setTypeRegistry(typeRegistry);
        templatedMap.setCollectionType(typeA);

        CompositeType typeReferer = typeRegistry.register(MockReferer.class);
        CompositeType typeReferee = typeRegistry.getType(MockReferee.class);
        MapType mapReferer = new MapType();
        mapReferer.setTypeRegistry(typeRegistry);
        mapReferer.setCollectionType(typeReferer);

        MapType mapReferee = new MapType();
        mapReferee.setTypeRegistry(typeRegistry);
        mapReferee.setCollectionType(typeReferee);

        configurationPersistenceManager.register("sample", mapA);
        configurationPersistenceManager.register("template", templatedMap);
        configurationPersistenceManager.register("referer", mapReferer);
        configurationPersistenceManager.register("referee", mapReferee);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testInsertIntoCollection()
    {
        MockA a = new MockA("a");
        configurationTemplateManager.insert("sample", a);

        MockA loaded = (MockA) configurationTemplateManager.getInstance("sample/a");
        assertNotNull(loaded);
        assertEquals("a", loaded.getName());
        assertEquals(null, loaded.getB());
    }

    public void testInsertIntoObject()
    {
        MockA a = new MockA("a");
        configurationTemplateManager.insert("sample", a);

        MockB b = new MockB("b");
        configurationTemplateManager.insert("sample/a/mock", b);

        MockB loaded = (MockB) configurationTemplateManager.getInstance("sample/a/mock");
        assertNotNull(loaded);
        assertEquals("b", loaded.getB());
    }

    public void testInsertExistingPath()
    {
        MockA a = new MockA("a");
        configurationTemplateManager.insert("sample", a);

        MockB b = new MockB("b");
        configurationTemplateManager.insert("sample/a/mock", b);

        try
        {
            configurationTemplateManager.insert("sample/a/mock", b);
            fail();
        }
        catch(IllegalArgumentException e)
        {
            assertEquals("Invalid insertion path 'sample/a/mock': record already exists (use save to modify)", e.getMessage());
        }
    }

    public void testSave()
    {
        MockA a = new MockA("a");
        String path = configurationTemplateManager.insert("sample", a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        a = configurationTemplateManager.deepClone(a);
        a.setB("somevalue");
        configurationTemplateManager.save(a);

        MockA loaded = (MockA) configurationTemplateManager.getInstance("sample/a");
        assertNotNull(loaded);
        assertEquals("a", loaded.getName());
        assertEquals("somevalue", loaded.getB());
    }

    public void testSaveIsDeep()
    {
        MockA a = new MockA("a");
        a.setMock(new MockB("b"));
        String path = configurationTemplateManager.insert("sample", a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals("b", a.getMock().getB());

        a = configurationTemplateManager.deepClone(a);
        a.getMock().setB("c");
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals("c", a.getMock().getB());
    }

    public void testSaveInsertsTransitively()
    {
        MockA a = new MockA("a");
        String path = configurationTemplateManager.insert("sample", a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals(0, a.getCs().size());

        a = configurationTemplateManager.deepClone(a);
        MockD d = new MockD("d");
        MockC c = new MockC("c");
        c.setD(d);
        a.getCs().put("c", c);
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals(1, a.getCs().size());
        c = a.getCs().get("c");
        assertNotNull(c);
        d = c.getD();
        assertNotNull(d);
        assertEquals("d", d.getName());
    }

    public void testSaveSavesTransitively()
    {
        MockD d = new MockD("d");
        MockC c = new MockC("c");
        c.setD(d);
        MockA a = new MockA("a");
        a.getCs().put("c", c);

        String path = configurationTemplateManager.insert("sample", a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals(1, a.getCs().size());
        c = a.getCs().get("c");
        assertNotNull(c);
        d = c.getD();
        assertNotNull(d);
        assertEquals("d", d.getName());

        a = configurationTemplateManager.deepClone(a);
        a.getCs().get("c").getD().setName("newname");
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals("newname", a.getCs().get("c").getD().getName());
    }

    public void testSaveChildObjectAdded()
    {
        MockA a = new MockA("a");
        String path = configurationTemplateManager.insert("sample", a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertNull(a.getMock());

        a = configurationTemplateManager.deepClone(a);
        a.setMock(new MockB("b"));
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertNotNull(a.getMock());
        assertEquals("b", a.getMock().getB());
    }

    public void testSaveChildObjectDeleted()
    {
        MockA a = new MockA("a");
        a.setMock(new MockB("b"));
        String path = configurationTemplateManager.insert("sample", a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertNotNull(a.getMock());
        assertEquals("b", a.getMock().getB());

        a = configurationTemplateManager.deepClone(a);
        a.setMock(null);
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertNull(a.getMock());
    }

    public void testSaveCollectionElementAdded()
    {
        MockA a = new MockA("a");
        String path = configurationTemplateManager.insert("sample", a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals(0, a.getCs().size());

        a = configurationTemplateManager.deepClone(a);
        a.getCs().put("jim", new MockC("jim"));
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals(1, a.getCs().size());
        assertNotNull(a.getCs().get("jim"));
    }

    public void testSaveCollectionElementRemoved()
    {
        MockA a = new MockA("a");
        a.getCs().put("jim", new MockC("jim"));
        String path = configurationTemplateManager.insert("sample", a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals(1, a.getCs().size());
        assertNotNull(a.getCs().get("jim"));

        a = configurationTemplateManager.deepClone(a);
        a.getCs().remove("jim");
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals(0, a.getCs().size());
    }

    public void testRename()
    {
        MockA a = new MockA("a");
        String path = configurationTemplateManager.insert("sample", a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertNotNull(a);

        // change the ID field, effectively triggering a rename on save.
        a = configurationTemplateManager.deepClone(a);
        a.setName("b");
        configurationTemplateManager.save(a);

        assertNull(configurationTemplateManager.getInstance("sample/a"));

        MockA loaded = (MockA) configurationTemplateManager.getInstance("sample/b");
        assertNotNull(loaded);

        assertEquals("b", loaded.getName());
    }

    public void testAllInsertEventsAreGenerated()
    {
        final List<ConfigurationEvent> events = new LinkedList<ConfigurationEvent>();
        eventManager.register(new EventListener()
        {
            public void handleEvent(Event evt)
            {
                events.add((ConfigurationEvent) evt);
            }

            public Class[] getHandledEvents()
            {
                return new Class[]{ConfigurationEvent.class};
            }
        });

        MockA a = new MockA("a");
        a.setMock(new MockB("b"));

        configurationTemplateManager.insert("sample", a);

        assertEquals(2, events.size());
        assertTrue(events.get(0) instanceof PostInsertEvent);
        assertEquals("sample/a", events.get(0).getInstance().getConfigurationPath());
        assertTrue(events.get(1) instanceof PostInsertEvent);
        assertEquals("sample/a/mock", events.get(1).getInstance().getConfigurationPath());
    }

    public void testSaveRecord()
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "avalue");
        record.put("b", "bvalue");

        configurationTemplateManager.insertRecord("sample", record);

        record = typeA.createNewRecord(false);
        record.put("name", "avalue");
        record.put("b", "newb");

        configurationTemplateManager.saveRecord("sample/avalue", record);

        Record loaded = configurationTemplateManager.getRecord("sample/avalue");
        assertEquals("newb", loaded.get("b"));
    }

    public void testSaveRecordUnknownPath()
    {
        MutableRecord record = typeA.createNewRecord(false);
        record.put("name", "avalue");
        record.put("b", "newb");

        try
        {
            configurationTemplateManager.saveRecord("sample/avalue", record);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Illegal path 'sample/avalue': no existing record found", e.getMessage());
        }
    }

    public void testSaveRecordToCollectionPath()
    {
        MutableRecord record = typeA.createNewRecord(false);
        record.put("name", "avalue");
        record.put("b", "newb");

        try
        {
            configurationTemplateManager.saveRecord("sample", record);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Illegal path 'sample': no parent record", e.getMessage());
        }
    }

    public void testSaveRecordDoesNotRemoveKeys()
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "avalue");
        record.put("b", "bvalue");
        record.put("c", "cvalue");

        configurationTemplateManager.insertRecord("sample", record);

        record = typeA.createNewRecord(false);
        record.put("name", "avalue");
        record.put("b", "newb");
        record.remove("c");
        configurationTemplateManager.saveRecord("sample/avalue", record);

        Record loaded = configurationTemplateManager.getRecord("sample/avalue");
        assertEquals("newb", loaded.get("b"));
        assertEquals("cvalue", loaded.get("c"));
    }

    public void testDeepClone()
    {
        MockA a = new MockA("aburger");
        MockB b = new MockB("bburger");
        a.setMock(b);

        configurationTemplateManager.insert("sample", a);

        a = configurationTemplateManager.getInstance("sample/aburger", MockA.class);
        MockA aClone = configurationTemplateManager.deepClone(a);

        assertNotSame(a, aClone);
        assertNotSame(a.getMock(), aClone.getMock());
        assertEquals(a.getHandle(), aClone.getHandle());
        assertEquals(a.getConfigurationPath(), aClone.getConfigurationPath());
        assertEquals("aburger", aClone.getName());
        assertEquals("bburger", aClone.getMock().getB());
    }

    public void testDeepCloneWithReferences()
    {
        MockReferee ee = new MockReferee("ee");
        configurationTemplateManager.insert("referee", ee);
        ee = configurationTemplateManager.getInstance("referee/ee", MockReferee.class);

        MockReferer er = new MockReferer("er");
        er.setRefToRef(ee);
        er.getRefToRefs().add(ee);

        configurationTemplateManager.insert("referer", er);
        er = configurationTemplateManager.getInstance("referer/er", MockReferer.class);
        ee = configurationTemplateManager.getInstance("referee/ee", MockReferee.class);

        MockReferer clone = configurationTemplateManager.deepClone(er);

        assertNotSame(er, clone);
        assertSame(ee, clone.getRefToRef());
        assertSame(ee, clone.getRefToRefs().get(0));
        assertEquals("er", er.getName());
    }

    public void testValidate() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        Configuration instance = configurationTemplateManager.validate("template", "a", record, false);
        List<String> aErrors = instance.getFieldErrors("name");
        assertEquals(1, aErrors.size());
        assertEquals("name requires a value", aErrors.get(0));
    }

    public void testValidateNullParentPath() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "value");
        Configuration instance = configurationTemplateManager.validate(null, "a", record, false);
        assertTrue(instance.isValid());
    }

    public void testValidateNullBaseName() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "value");
        Configuration instance = configurationTemplateManager.validate("template", null, record, false);
        assertTrue(instance.isValid());
    }

    public void testValidateTemplate() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "a");
        configurationTemplateManager.markAsTemplate(record);
        configurationTemplateManager.insertRecord("template", record);

        record = typeB.createNewRecord(false);
        MockB instance = configurationTemplateManager.validate("template/a", "mock", record, false);
        assertTrue(instance.isValid());
    }

    public void testValidateTemplateIdStillRequired() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        configurationTemplateManager.markAsTemplate(record);
        MockA instance = configurationTemplateManager.validate("template", "", record, false);
        assertFalse(instance.isValid());
        final List<String> errors = instance.getFieldErrors("name");
        assertEquals(1, errors.size());
        assertEquals("name requires a value", errors.get(0));
    }

    public void testValidateNestedPath() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "a");
        configurationTemplateManager.insertRecord("template", record);

        record = typeB.createNewRecord(true);
        Configuration instance = configurationTemplateManager.validate("template/a", "b", record, false);
        List<String> aErrors = instance.getFieldErrors("b");
        assertEquals(1, aErrors.size());
        assertEquals("b requires a value", aErrors.get(0));
    }

    public void testValidateTemplateNestedPath() throws TypeException
    {
        // Check that a record not directly marked us a template is correctly
        // detected as a template for validation (by checking the owner).
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "a");
        configurationTemplateManager.markAsTemplate(record);
        configurationTemplateManager.insertRecord("template", record);

        record = typeB.createNewRecord(true);
        MockB instance = configurationTemplateManager.validate("template/a", "b", record, false);
        assertTrue(instance.isValid());
        assertNull(instance.getB());
    }

    public void testCachedInstancesAreValidated() throws TypeException
    {
        MockA a = new MockA("a");
        a.setMock(new MockB());
        configurationTemplateManager.insert("sample", a);

        MockB instance = configurationTemplateManager.getInstance("sample/a/mock", MockB.class);
        final List<String> errors = instance.getFieldErrors("b");
        assertEquals(1, errors.size());
        assertEquals("b requires a value", errors.get(0));
    }

    public void testCachedTemplateInstancesAreValidated() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "a+");
        configurationTemplateManager.markAsTemplate(record);
        configurationTemplateManager.insertRecord("template", record);

        MockA instance = configurationTemplateManager.getInstance("template/a+", MockA.class);
        final List<String> errors = instance.getFieldErrors("name");
        assertEquals(1, errors.size());
        assertEquals("name must consist of an alphanumeric character followed by zero or more alphanumeric characters mixed with characters ' ', '.', '-' or '_'", errors.get(0));
    }

    public void testCachedTemplateInstancesAreValidatedAsTemplates() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "a");
        configurationTemplateManager.markAsTemplate(record);
        configurationTemplateManager.insertRecord("template", record);

        configurationTemplateManager.insert("template/a/mock", new MockB());

        MockB instance = configurationTemplateManager.getInstance("template/a/mock", MockB.class);
        assertTrue(instance.isValid());
    }

    public void testValidateNotDeep() throws TypeException
    {
        MockA a = new MockA("a");
        a.setMock(new MockB());
        MutableRecord record = typeA.unstantiate(a);

        MockA instance = configurationTemplateManager.validate("sample", null, record, false);
        assertTrue(instance.isValid());
        assertTrue(instance.getMock().isValid());
    }

    public void testValidateDeep() throws TypeException
    {
        MockA a = new MockA("a");
        a.setMock(new MockB());
        MutableRecord record = typeA.unstantiate(a);

        MockA instance = configurationTemplateManager.validate("sample", null, record, true);
        assertTrue(instance.isValid());
        assertFalse(instance.getMock().isValid());
    }

    public void testValidateDeepNestedList() throws TypeException
    {
        MockA a = new MockA("a");
        a.getDs().add(new MockD());
        Record record = typeA.unstantiate(a);

        MockA validated = configurationTemplateManager.validate("sample", null, record, true);
        assertTrue(validated.isValid());
        MockD mockD = validated.getDs().get(0);
        assertMissingName(mockD);
    }

    public void testValidateDeepNestedMap() throws TypeException
    {
        MockA a = new MockA("a");
        a.getCs().put("name", new MockC());
        Record record = typeA.unstantiate(a);

        MockA validated = configurationTemplateManager.validate("sample", null, record, true);
        assertTrue(validated.isValid());
        MockC mockC = validated.getCs().get("name");
        assertMissingName(mockC);
    }

    public void testIsTemplatedCollection()
    {
        assertFalse(configurationTemplateManager.isTemplatedCollection("sample"));
        assertTrue(configurationTemplateManager.isTemplatedCollection("template"));
    }

    public void testIsTemplatedCollectionUnknownPath()
    {
        assertFalse(configurationTemplateManager.isTemplatedCollection("unknown"));
    }

    public void testIsTemplatedCollectionChildPath()
    {
        assertFalse(configurationTemplateManager.isTemplatedCollection("template/a"));

        MockA a = new MockA("a");
        configurationTemplateManager.insert("template", a);

        assertFalse(configurationTemplateManager.isTemplatedCollection("template/a"));
    }

    public void testDelete()
    {
        MockA a = new MockA("mock");
        String path = configurationTemplateManager.insert("sample", a);

        configurationTemplateManager.delete(path);

        // Are both record and instance gone?
        assertNoSuchPath(path);
        assertEmptyMap("sample");
    }

    public void testDeleteAllTrivial()
    {
        MockA a = new MockA("mock");
        String path = configurationTemplateManager.insert("sample", a);

        assertEquals(1, configurationTemplateManager.deleteAll(path));

        // Are both record and instance gone?
        assertNoSuchPath(path);
        assertEmptyMap("sample");
    }

    public void testDeleteAllNoMatches()
    {
        assertEquals(0, configurationTemplateManager.deleteAll("sample/none"));
    }

    public void testDeleteAllMultipleMatches()
    {
        MockA a1 = new MockA("a1");
        MockA a2 = new MockA("a2");
        String path1 = configurationTemplateManager.insert("sample", a1);
        String path2 = configurationTemplateManager.insert("sample", a2);

        assertEquals(2, configurationTemplateManager.deleteAll("sample/*"));

        assertNoSuchPath(path1);
        assertNoSuchPath(path2);
        assertEmptyMap("sample");
    }

    public void testPathExistsEmptyPath()
    {
        assertFalse(configurationTemplateManager.pathExists(""));
    }

    public void testPathExistsNonExistantScope()
    {
        assertFalse(configurationTemplateManager.pathExists("nosuchscope"));
    }

    public void testPathExistsScopeExistsPathDoesnt()
    {
        assertFalse(configurationTemplateManager.pathExists("sample/nosuchpath"));
    }

    public void testPathExistsExistantScope()
    {
        assertTrue(configurationTemplateManager.pathExists("sample"));
    }

    public void testPathExistsExistantPath()
    {
        MockA a = new MockA("mock");
        String path = configurationTemplateManager.insert("sample", a);
        assertTrue(configurationTemplateManager.pathExists(path));
    }

    private void assertNoSuchPath(String path)
    {
        assertFalse(configurationTemplateManager.pathExists(path));
        assertNull(configurationTemplateManager.getRecord(path));
        assertNull(configurationTemplateManager.getInstance(path));
    }

    private void assertEmptyMap(String path)
    {
        // Are they removed from the parent record and instance?
        assertEquals(0, configurationTemplateManager.getRecord(path).size());
        assertEquals(0, ((Map) configurationTemplateManager.getInstance(path)).size());
    }

    private void assertMissingName(NamedConfiguration instance)
    {
        assertFalse(instance.isValid());
        List<String> fieldErrors = instance.getFieldErrors("name");
        assertEquals(1, fieldErrors.size());
        assertEquals("name requires a value", fieldErrors.get(0));
    }

    @SymbolicName("mockA")
    public static class MockA extends AbstractNamedConfiguration
    {
        private String b;
        private String c;

        private MockB mock;
        private Map<String, MockC> cs = new HashMap<String, MockC>();
        private List<MockD> ds = new LinkedList<MockD>();
        private List<String> pl = new LinkedList<String>();

        public MockA()
        {
        }

        public MockA(String name)
        {
            super(name);
        }

        public String getB()
        {
            return b;
        }

        public void setB(String b)
        {
            this.b = b;
        }

        public String getC()
        {
            return c;
        }

        public void setC(String c)
        {
            this.c = c;
        }

        public MockB getMock()
        {
            return mock;
        }

        public void setMock(MockB mock)
        {
            this.mock = mock;
        }

        public Map<String, MockC> getCs()
        {
            return cs;
        }

        public void setCs(Map<String, MockC> cs)
        {
            this.cs = cs;
        }

        public List<MockD> getDs()
        {
            return ds;
        }

        public void setDs(List<MockD> ds)
        {
            this.ds = ds;
        }

        public List<String> getPl()
        {
            return pl;
        }

        public void setPl(List<String> pl)
        {
            this.pl = pl;
        }
    }

    @SymbolicName("mockB")
    public static class MockB extends AbstractConfiguration
    {
        @Required
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

    @SymbolicName("mockC")
    public static class MockC extends AbstractNamedConfiguration
    {
        private MockD d;

        public MockC()
        {
        }

        public MockC(String name)
        {
            super(name);
        }

        public MockD getD()
        {
            return d;
        }

        public void setD(MockD d)
        {
            this.d = d;
        }
    }

    @SymbolicName("mockD")
    public static class MockD extends AbstractNamedConfiguration
    {
        public MockD()
        {
        }

        public MockD(String name)
        {
            super(name);
        }
    }

    @SymbolicName("mockReferer")
    public static class MockReferer extends AbstractNamedConfiguration
    {
        MockReferee ref;
        @Reference
        MockReferee refToRef;
        @Reference
        List<MockReferee> refToRefs = new LinkedList<MockReferee>();

        public MockReferer()
        {
        }

        public MockReferer(String name)
        {
            super(name);
        }

        public MockReferee getRef()
        {
            return ref;
        }

        public void setRef(MockReferee ref)
        {
            this.ref = ref;
        }

        public MockReferee getRefToRef()
        {
            return refToRef;
        }

        public void setRefToRef(MockReferee refToRef)
        {
            this.refToRef = refToRef;
        }

        public List<MockReferee> getRefToRefs()
        {
            return refToRefs;
        }

        public void setRefToRefs(List<MockReferee> refToRefs)
        {
            this.refToRefs = refToRefs;
        }
    }

    @SymbolicName("mockReferee")
    public static class MockReferee extends AbstractNamedConfiguration
    {
        public MockReferee()
        {
        }

        public MockReferee(String name)
        {
            super(name);
        }
    }
}
