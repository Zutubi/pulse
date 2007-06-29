package com.zutubi.prototype.config;

import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.config.events.PostInsertEvent;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.TemplatedMapType;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.validation.ValidationAwareSupport;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

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

        typeA = typeRegistry.register("mockA", MockA.class);
        typeB = typeRegistry.getType("mockB");
        MapType mapA = new MapType();
        mapA.setTypeRegistry(typeRegistry);
        mapA.setCollectionType(typeA);

        MapType templatedMap = new TemplatedMapType();
        templatedMap.setTypeRegistry(typeRegistry);
        templatedMap.setCollectionType(typeA);

        configurationPersistenceManager.register("sample", mapA);
        configurationPersistenceManager.register("template", templatedMap);
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
        assertEquals("a", loaded.getA());
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

    public void testSave()
    {
        MockA a = new MockA("a");
        configurationTemplateManager.insert("sample", a);

        a.setB("somevalue");
        configurationTemplateManager.save("sample/a", a);

        MockA loaded = (MockA) configurationTemplateManager.getInstance("sample/a");
        assertNotNull(loaded);
        assertEquals("a", loaded.getA());
        assertEquals("somevalue", loaded.getB());
    }

    public void testRename()
    {
        MockA a = new MockA("a");
        configurationTemplateManager.insert("sample", a);

        assertNotNull(configurationTemplateManager.getInstance("sample/a"));

        // change the ID field, effectively triggering a rename on save.
        a.setA("b");
        configurationTemplateManager.save("sample/a", a);

        assertNull(configurationTemplateManager.getInstance("sample/a"));

        MockA loaded = (MockA) configurationTemplateManager.getInstance("sample/b");
        assertNotNull(loaded);

        assertEquals("b", loaded.getA());
    }

    public void testAllInsertEventsAreGenerated()
    {
        final List<ConfigurationEvent> events = new LinkedList<ConfigurationEvent>();
        eventManager.register(new EventListener()
        {
            public void handleEvent(Event evt)
            {
                events.add((ConfigurationEvent)evt);
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
        record.put("a", "avalue");
        record.put("b", "bvalue");

        configurationTemplateManager.insertRecord("sample", record);

        record = typeA.createNewRecord(false);
        record.put("a", "avalue");
        record.put("b", "newb");

        configurationTemplateManager.saveRecord("sample/avalue", record);

        Record loaded = configurationTemplateManager.getRecord("sample/avalue");
        assertEquals("newb", loaded.get("b"));
    }

    public void testSaveRecordUnknownPath()
    {
        MutableRecord record = typeA.createNewRecord(false);
        record.put("a", "avalue");
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
        record.put("a", "avalue");
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
        record.put("a", "avalue");
        record.put("b", "bvalue");
        record.put("c", "cvalue");

        configurationTemplateManager.insertRecord("sample", record);

        record = typeA.createNewRecord(false);
        record.put("a", "avalue");
        record.put("b", "newb");
        record.remove("c");
        configurationTemplateManager.saveRecord("sample/avalue", record);

        Record loaded = configurationTemplateManager.getRecord("sample/avalue");
        assertEquals("newb", loaded.get("b"));
        assertEquals("cvalue", loaded.get("c"));
    }

    public void testValidate()
    {
        MutableRecord record = typeA.createNewRecord(true);
        ValidationAwareSupport validationAware = new ValidationAwareSupport();
        assertNull(configurationTemplateManager.validate("template", "a", record, validationAware));
        List<String> aErrors = validationAware.getFieldErrors("a");
        assertEquals(1, aErrors.size());
        assertEquals("a requires a value", aErrors.get(0));
    }

    public void testValidateNullParentPath()
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("a", "value");
        ValidationAwareSupport validationAware = new ValidationAwareSupport();
        assertNotNull(configurationTemplateManager.validate(null, "a", record, validationAware));
    }

    public void testValidateNullBaseName()
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("a", "value");
        ValidationAwareSupport validationAware = new ValidationAwareSupport();
        assertNotNull(configurationTemplateManager.validate("template", null, record, validationAware));
    }

    public void testValidateTemplate()
    {
        MutableRecord record = typeA.createNewRecord(true);
        configurationTemplateManager.markAsTemplate(record);
        ValidationAwareSupport validationAware = new ValidationAwareSupport();
        MockA instance = configurationTemplateManager.validate("template", "a", record, validationAware);
        assertNotNull(instance);
        assertNull(instance.getA());
        assertFalse(validationAware.hasErrors());
    }

    public void testValidateNestedPath()
    {
        // Check that a record not directly marked us a template is correctly
        // detected as a template for validation (by checking the owner).
        MutableRecord record = typeA.createNewRecord(true);
        record.put("a", "a");
        configurationTemplateManager.insertRecord("template", record);

        record = typeB.createNewRecord(true);
        ValidationAwareSupport validationAware = new ValidationAwareSupport();
        assertNull(configurationTemplateManager.validate("template/a", "b", record, validationAware));
        List<String> aErrors = validationAware.getFieldErrors("b");
        assertEquals(1, aErrors.size());
        assertEquals("b requires a value", aErrors.get(0));
    }
    
    public void testValidateTemplateNestedPath()
    {
        // Check that a record not directly marked us a template is correctly
        // detected as a template for validation (by checking the owner).
        MutableRecord record = typeA.createNewRecord(true);
        record.put("a", "a");
        configurationTemplateManager.markAsTemplate(record);
        configurationTemplateManager.insertRecord("template", record);

        record = typeB.createNewRecord(true);
        ValidationAwareSupport validationAware = new ValidationAwareSupport();
        MockB instance = configurationTemplateManager.validate("template/a", "b", record, validationAware);
        assertNotNull(instance);
        assertNull(instance.getB());
        assertFalse(validationAware.hasErrors());
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

    @SymbolicName("mockA")
    public static class MockA extends AbstractConfiguration
    {
        @ID
        private String a;
        private String b;
        private String c;

        private MockB mock;

        public MockA(){}
        public MockA(String a){this.a = a;}
        public String getA(){return a;}
        public void setA(String a){this.a = a;}
        public String getB(){return b;}
        public void setB(String b){this.b = b;}
        public String getC(){return c;}
        public void setC(String c){this.c = c;}

        public MockB getMock(){return mock;}
        public void setMock(MockB mock){this.mock = mock;}
    }

    @SymbolicName("mockB")
    public static class MockB extends AbstractConfiguration
    {
        @Required
        private String b;

        public MockB(){}
        public MockB(String b){this.b = b;}
        public String getB(){return b;}
        public void setB(String b){this.b = b;}
    }
}
