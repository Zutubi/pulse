package com.zutubi.tove.config;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.tove.annotations.ID;
import com.zutubi.tove.annotations.ReadOnly;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.api.NamedConfiguration;
import com.zutubi.tove.config.events.*;
import com.zutubi.tove.security.*;
import com.zutubi.tove.transaction.UserTransaction;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.TemplatedMapType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.FalsePredicate;
import com.zutubi.util.Pair;
import com.zutubi.util.Predicate;
import com.zutubi.util.TruePredicate;
import com.zutubi.validation.annotations.Required;

import java.util.*;

public class ConfigurationTemplateManagerTest extends AbstractConfigurationSystemTestCase
{
    private static final String SCOPE_SAMPLE = "sample";
    private static final String SCOPE_TEMPLATED = "template";
    private static final String SCOPE_REFERER = "referer";
    private static final String SCOPE_REFEREE = "referee";

    private static final String GRANDPARENT_NAME = "gp";
    private static final String CHILD_NAME = "c";
    private static final String GRANDCHILD_NAME = "gc";
    
    private CompositeType typeA;
    private CompositeType typeB;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeA = typeRegistry.register(MockA.class);
        typeB = typeRegistry.getType(MockB.class);
        MapType mapA = new MapType(typeA, typeRegistry);

        MapType templatedMap = new TemplatedMapType(typeA, typeRegistry);

        CompositeType typeReferer = typeRegistry.register(MockReferer.class);
        CompositeType typeReferee = typeRegistry.getType(MockReferee.class);
        MapType mapReferer = new MapType(typeReferer, typeRegistry);
        MapType mapReferee = new MapType(typeReferee, typeRegistry);

        configurationPersistenceManager.register(SCOPE_SAMPLE, mapA);
        configurationPersistenceManager.register(SCOPE_TEMPLATED, templatedMap);
        configurationPersistenceManager.register(SCOPE_REFERER, mapReferer);
        configurationPersistenceManager.register(SCOPE_REFEREE, mapReferee);

        accessManager.registerAuthorityProvider(MockA.class, new AuthorityProvider<MockA>()
        {
            public Set<String> getAllowedAuthorities(String action, MockA resource)
            {
                return new HashSet<String>(Arrays.asList(action));
            }
        });
    }

    public void testInsertIntoCollection()
    {
        MockA a = new MockA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        MockA loaded = (MockA) configurationTemplateManager.getInstance("sample/a");
        assertNotNull(loaded);
        assertEquals("a", loaded.getName());
        assertEquals(null, loaded.getB());
    }

    public void testInsertIntoObject()
    {
        MockA a = new MockA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        MockB b = new MockB("b");
        configurationTemplateManager.insert("sample/a/mock", b);

        MockB loaded = (MockB) configurationTemplateManager.getInstance("sample/a/mock");
        assertNotNull(loaded);
        assertEquals("b", loaded.getB());
    }

    public void testInsertExistingPath()
    {
        MockA a = new MockA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        MockB b = new MockB("b");
        configurationTemplateManager.insert("sample/a/mock", b);

        try
        {
            configurationTemplateManager.insert("sample/a/mock", b);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Invalid insertion path 'sample/a/mock': record already exists (use save to modify)", e.getMessage());
        }
    }

    public void testInsertNoPermission()
    {
        configurationSecurityManager.registerGlobalPermission(SCOPE_SAMPLE, AccessManager.ACTION_CREATE, AccessManager.ACTION_CREATE);
        accessManager.setActorProvider(new ActorProvider()
        {
            public Actor getActor()
            {
                return new DefaultActor("test", AccessManager.ACTION_DELETE, AccessManager.ACTION_VIEW, AccessManager.ACTION_WRITE);
            }
        });

        try
        {
            configurationTemplateManager.insert(SCOPE_SAMPLE, new MockA("a"));
            fail();
        }
        catch (Exception e)
        {
            assertEquals("Permission to create at path 'sample' denied", e.getMessage());
        }
    }

    public void testSave()
    {
        MockA a = new MockA("a");
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

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
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

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
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

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

        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

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
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

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
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

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
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

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
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals(1, a.getCs().size());
        assertNotNull(a.getCs().get("jim"));

        a = configurationTemplateManager.deepClone(a);
        a.getCs().remove("jim");
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals(0, a.getCs().size());
    }

    public void testSaveNoPermission()
    {
        configurationTemplateManager.insert(SCOPE_SAMPLE, new MockA("a"));

        accessManager.setActorProvider(new ActorProvider()
        {
            public Actor getActor()
            {
                return new DefaultActor("test", AccessManager.ACTION_CREATE, AccessManager.ACTION_DELETE, AccessManager.ACTION_VIEW);
            }
        });

        try
        {
            configurationTemplateManager.save(configurationTemplateManager.getInstance("sample/a"));

            fail();
        }
        catch (Exception e)
        {
            assertEquals("Permission to write at path 'sample/a' denied", e.getMessage());
        }
    }


    public void testRename()
    {
        MockA a = new MockA("a");
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

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
        eventManager.register(new com.zutubi.events.EventListener()
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

        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        assertEquals(4, events.size());
        assertTrue(events.get(0) instanceof InsertEvent);
        assertEquals("sample/a", events.get(0).getInstance().getConfigurationPath());
        assertTrue(events.get(1) instanceof InsertEvent);
        assertEquals("sample/a/mock", events.get(1).getInstance().getConfigurationPath());
        assertTrue(events.get(2) instanceof PostInsertEvent);
        assertEquals("sample/a", events.get(2).getInstance().getConfigurationPath());
        assertTrue(events.get(3) instanceof PostInsertEvent);
        assertEquals("sample/a/mock", events.get(3).getInstance().getConfigurationPath());
    }

    public void testSaveRecord()
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "avalue");
        record.put("b", "bvalue");

        configurationTemplateManager.insertRecord(SCOPE_SAMPLE, record);

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
            configurationTemplateManager.saveRecord(SCOPE_SAMPLE, record);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Illegal path 'sample': attempt to save a collection", e.getMessage());
        }
    }

    public void testSaveRecordDoesNotRemoveKeys()
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "avalue");
        record.put("b", "bvalue");
        record.put("c", "cvalue");

        configurationTemplateManager.insertRecord(SCOPE_SAMPLE, record);

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

        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

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
        configurationTemplateManager.insert(SCOPE_REFEREE, ee);
        ee = configurationTemplateManager.getInstance("referee/ee", MockReferee.class);

        MockReferer er = new MockReferer("er");
        er.setRefToRef(ee);
        er.getRefToRefs().add(ee);

        configurationTemplateManager.insert(SCOPE_REFERER, er);
        er = configurationTemplateManager.getInstance("referer/er", MockReferer.class);
        ee = configurationTemplateManager.getInstance("referee/ee", MockReferee.class);

        MockReferer clone = configurationTemplateManager.deepClone(er);

        assertNotSame(er, clone);
        assertSame(ee, clone.getRefToRef());
        assertSame(ee, clone.getRefToRefs().get(0));
        assertEquals("er", er.getName());
    }

    public void testDeepCloneWithInternalReference()
    {
        // We need to jump through hoops a bit to set this up.  It is not
        // possible to set up a reference to something until it is saved.
        // Hence we need to insert the referer and nested referee first, and
        // then later we can add references to the nested referee.
        MockReferee ee = new MockReferee("ee");
        MockReferer er = new MockReferer("er");
        er.setRef(ee);
        configurationTemplateManager.insert(SCOPE_REFERER, er);
        er = configurationTemplateManager.getInstance("referer/er", MockReferer.class);
        er = configurationTemplateManager.deepClone(er);
        ee = er.getRef();
        er.setRefToRef(ee);
        er.getRefToRefs().add(ee);
        configurationTemplateManager.save(er);

        er = configurationTemplateManager.getInstance("referer/er", MockReferer.class);
        ee = er.getRef();
        assertSame(ee, er.getRefToRef());
        assertSame(ee, er.getRefToRefs().get(0));

        // Now we can actually clone and test.
        MockReferer clone = configurationTemplateManager.deepClone(er);
        MockReferee refClone = clone.getRef();

        assertNotSame(er, clone);
        assertNotSame(ee, refClone);
        assertSame(refClone, clone.getRefToRef());
        assertSame(refClone, clone.getRefToRefs().get(0));
    }

    public void testDeepClonePreservesPaths() throws TypeException
    {
        MockA a = new MockA("a");
        a.setMock(new MockB("b"));
        a.getCs().put("c", new MockC("c"));
        a.getDs().add(new MockD("d"));
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = (MockA) configurationTemplateManager.getInstance("sample/a");

        MockA clone = configurationTemplateManager.deepClone(a);
        assertEquals(a.getConfigurationPath(), clone.getConfigurationPath());
        assertEquals(a.getMock().getConfigurationPath(), clone.getMock().getConfigurationPath());
        assertEquals(a.getCs().get("c").getConfigurationPath(), clone.getCs().get("c").getConfigurationPath());
        assertEquals(a.getDs().get(0).getConfigurationPath(), clone.getDs().get(0).getConfigurationPath());
    }

    public void testDeepClonePreservesHandles() throws TypeException
    {
        MockA a = new MockA("a");
        a.setMock(new MockB("b"));
        a.getCs().put("c", new MockC("c"));
        a.getDs().add(new MockD("d"));
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = (MockA) configurationTemplateManager.getInstance("sample/a");

        MockA clone = configurationTemplateManager.deepClone(a);
        assertEquals(a.getHandle(), clone.getHandle());
        assertEquals(a.getMock().getHandle(), clone.getMock().getHandle());
        assertEquals(a.getCs().get("c").getHandle(), clone.getCs().get("c").getHandle());
        assertEquals(a.getDs().get(0).getHandle(), clone.getDs().get(0).getHandle());
    }

    public void testDeepCloneAndSavePreservesMeta() throws TypeException
    {
        MockA a = new MockA("a");
        MutableRecord record = typeA.unstantiate(a);
        record.putMeta("testkey", "value");

        String path = configurationTemplateManager.insertRecord(SCOPE_SAMPLE, record);
        Record savedRecord = configurationTemplateManager.getRecord(path);
        assertEquals("value", savedRecord.getMeta("testkey"));

        a = configurationTemplateManager.getInstance(path, MockA.class);
        a = configurationTemplateManager.deepClone(a);
        configurationTemplateManager.save(a);

        savedRecord = configurationTemplateManager.getRecord(path);
        assertEquals("value", savedRecord.getMeta("testkey"));
    }

    public void testValidate() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        Configuration instance = configurationTemplateManager.validate(SCOPE_TEMPLATED, "a", record, true, false);
        List<String> aErrors = instance.getFieldErrors("name");
        assertEquals(1, aErrors.size());
        assertEquals("name requires a value", aErrors.get(0));
    }

    public void testValidateNullParentPath() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "value");
        Configuration instance = configurationTemplateManager.validate(null, "a", record, true, false);
        assertTrue(instance.isValid());
    }

    public void testValidateNullBaseName() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "value");
        Configuration instance = configurationTemplateManager.validate(SCOPE_TEMPLATED, null, record, true, false);
        assertTrue(instance.isValid());
    }

    public void testValidateTemplate() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "a");
        configurationTemplateManager.markAsTemplate(record);
        configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);

        record = typeB.createNewRecord(false);
        MockB instance = configurationTemplateManager.validate("template/a", "mock", record, false, false);
        assertTrue(instance.isValid());
    }

    public void testValidateTemplateIdStillRequired() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        configurationTemplateManager.markAsTemplate(record);
        MockA instance = configurationTemplateManager.validate(SCOPE_TEMPLATED, "", record, false, false);
        assertFalse(instance.isValid());
        final List<String> errors = instance.getFieldErrors("name");
        assertEquals(1, errors.size());
        assertEquals("name requires a value", errors.get(0));
    }

    public void testValidateNestedPath() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "a");
        configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);

        record = typeB.createNewRecord(true);
        Configuration instance = configurationTemplateManager.validate("template/a", "b", record, true, false);
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
        configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);

        record = typeB.createNewRecord(true);
        MockB instance = configurationTemplateManager.validate("template/a", "b", record, false, false);
        assertTrue(instance.isValid());
        assertNull(instance.getB());
    }

    public void testCachedInstancesAreValidated() throws TypeException
    {
        MockA a = new MockA("a");
        a.setMock(new MockB());
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        MockB instance = configurationTemplateManager.getInstance("sample/a/mock", MockB.class);
        final List<String> errors = instance.getFieldErrors("b");
        assertEquals(1, errors.size());
        assertEquals("b requires a value", errors.get(0));
    }

    public void testCachedTemplateInstancesAreValidated() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "a$");
        configurationTemplateManager.markAsTemplate(record);
        configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);

        MockA instance = configurationTemplateManager.getInstance("template/a$", MockA.class);
        final List<String> errors = instance.getFieldErrors("name");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0), errors.get(0).contains("dollar sign"));
    }

    public void testCachedTemplateInstancesAreValidatedAsTemplates() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        record.put("name", "a");
        configurationTemplateManager.markAsTemplate(record);
        configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);

        configurationTemplateManager.insert("template/a/mock", new MockB());

        MockB instance = configurationTemplateManager.getInstance("template/a/mock", MockB.class);
        assertTrue(instance.isValid());
    }

    public void testValidateNotDeep() throws TypeException
    {
        MockA a = new MockA("a");
        a.setMock(new MockB());
        MutableRecord record = typeA.unstantiate(a);

        MockA instance = configurationTemplateManager.validate(SCOPE_SAMPLE, null, record, true, false);
        assertTrue(instance.isValid());
        assertTrue(instance.getMock().isValid());
    }

    public void testValidateDeep() throws TypeException
    {
        MockA a = new MockA("a");
        a.setMock(new MockB());
        MutableRecord record = typeA.unstantiate(a);

        MockA instance = configurationTemplateManager.validate(SCOPE_SAMPLE, null, record, true, true);
        assertTrue(instance.isValid());
        assertFalse(instance.getMock().isValid());
    }

    public void testValidateDeepNestedList() throws TypeException
    {
        MockA a = new MockA("a");
        a.getDs().add(new MockD());
        Record record = typeA.unstantiate(a);

        MockA validated = configurationTemplateManager.validate(SCOPE_SAMPLE, null, record, true, true);
        assertTrue(validated.isValid());
        MockD mockD = validated.getDs().get(0);
        assertMissingName(mockD);
    }

    public void testValidateDeepNestedMap() throws TypeException
    {
        MockA a = new MockA("a");
        a.getCs().put("name", new MockC());
        Record record = typeA.unstantiate(a);

        MockA validated = configurationTemplateManager.validate(SCOPE_SAMPLE, null, record, true, true);
        assertTrue(validated.isValid());
        MockC mockC = validated.getCs().get("name");
        assertMissingName(mockC);
    }

    public void testIsTemplatedCollection()
    {
        assertFalse(configurationTemplateManager.isTemplatedCollection(SCOPE_SAMPLE));
        assertTrue(configurationTemplateManager.isTemplatedCollection(SCOPE_TEMPLATED));
    }

    public void testIsTemplatedCollectionUnknownPath()
    {
        assertFalse(configurationTemplateManager.isTemplatedCollection("unknown"));
    }

    public void testIsTemplatedCollectionChildPath()
    {
        assertFalse(configurationTemplateManager.isTemplatedCollection("template/a"));

        MockA a = new MockA("a");
        configurationTemplateManager.insert(SCOPE_TEMPLATED, a);

        assertFalse(configurationTemplateManager.isTemplatedCollection("template/a"));
    }

    public void testCanDeleteInvalidPath()
    {
        assertFalse(configurationTemplateManager.canDelete("skgjkg"));
    }

    public void testCanDeleteScope()
    {
        assertFalse(configurationTemplateManager.canDelete(SCOPE_SAMPLE));
    }

    public void testCanDeletePermanent()
    {
        MockA a = new MockA("mock");
        a.setPermanent(true);
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertFalse(configurationTemplateManager.canDelete(path));
    }

    public void testCanDeleteInheritedComposite() throws TypeException
    {
        assertFalse(configurationTemplateManager.canDelete(PathUtils.getPath(insertInherited(), "mock")));
    }

    public void testCanDeleteSimple()
    {
        MockA a = new MockA("mock");
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertTrue(configurationTemplateManager.canDelete(path));
    }

    public void testCanDeleteCompositeChild()
    {
        MockA a = new MockA("mock");
        a.setMock(new MockB("b"));
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertTrue(configurationTemplateManager.canDelete(PathUtils.getPath(path, "mock")));
    }

    public void testCanDeleteMapItem()
    {
        MockA a = new MockA("mock");
        a.getCs().put("cee", new MockC("cee"));
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertTrue(configurationTemplateManager.canDelete(PathUtils.getPath(path, "cs/cee")));
    }

    public void testCanDeleteOwnedComposite() throws TypeException
    {
        insertInherited();
        assertTrue(configurationTemplateManager.canDelete("template/mock/mock"));
    }

    public void testCanDeleteInheritedMapItem() throws TypeException
    {
        assertTrue(configurationTemplateManager.canDelete(PathUtils.getPath(insertInherited(), "cs/cee")));
    }

    private String insertInherited() throws TypeException
    {
        MockA a = new MockA("mock");
        a.setMock(new MockB("bee"));
        a.getCs().put("cee", new MockC("cee"));
        MutableRecord record = typeA.unstantiate(a);
        configurationTemplateManager.markAsTemplate(record);
        String path = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);

        record = typeA.unstantiate(new MockA("child"));
        configurationTemplateManager.setParentTemplate(record, recordManager.select(path).getHandle());
        path = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);
        return path;
    }

    public void testDelete()
    {
        MockA a = new MockA("mock");
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        configurationTemplateManager.delete(path);

        // Are both record and instance gone?
        assertNoSuchPath(path);
        assertEmptyMap(SCOPE_SAMPLE);
    }

    public void testDeletePermanent()
    {
        try
        {
            MockA a = new MockA("mock");
            a.setPermanent(true);
            String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

            configurationTemplateManager.delete(path);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Cannot delete instance at path 'sample/mock': marked permanent", e.getMessage());
        }
    }

    public void testDeleteScope() throws TypeException
    {
        failedDeleteHelper(SCOPE_SAMPLE, "Invalid path 'sample': cannot delete a scope");
    }

    public void testDeleteInheritedComposite() throws TypeException
    {
        String path = PathUtils.getPath(insertInherited(), "mock");
        failedDeleteHelper(path, "Invalid path 'template/child/mock': cannot delete an inherited composite property");
    }

    public void testDeleteNoPermission()
    {
        configurationTemplateManager.insert(SCOPE_SAMPLE, new MockA("a"));
        configurationSecurityManager.registerGlobalPermission("sample/*", AccessManager.ACTION_DELETE, AccessManager.ACTION_DELETE);
        accessManager.setActorProvider(new ActorProvider()
        {
            public Actor getActor()
            {
                return new DefaultActor("test", AccessManager.ACTION_CREATE, AccessManager.ACTION_VIEW, AccessManager.ACTION_WRITE);
            }
        });

        failedDeleteHelper("sample/a", "Permission to delete at path 'sample/a' denied");
    }

    public void testDeleteNonExistant()
    {
        failedDeleteHelper("sample/nope", "No such path 'sample/nope'");
    }

    private void failedDeleteHelper(String path, String expectedMessage)
    {
        try
        {
            configurationTemplateManager.delete(path);
            fail();
        }
        catch (Exception e)
        {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    public void testDeleteAllTrivial()
    {
        MockA a = new MockA("mock");
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        assertEquals(1, configurationTemplateManager.deleteAll(path));

        // Are both record and instance gone?
        assertNoSuchPath(path);
        assertEmptyMap(SCOPE_SAMPLE);
    }

    public void testDeleteAllNoMatches()
    {
        assertEquals(0, configurationTemplateManager.deleteAll("sample/none"));
    }

    public void testDeleteAllMultipleMatches()
    {
        MockA a1 = new MockA("a1");
        MockA a2 = new MockA("a2");
        String path1 = configurationTemplateManager.insert(SCOPE_SAMPLE, a1);
        String path2 = configurationTemplateManager.insert(SCOPE_SAMPLE, a2);

        assertEquals(2, configurationTemplateManager.deleteAll("sample/*"));

        assertNoSuchPath(path1);
        assertNoSuchPath(path2);
        assertEmptyMap(SCOPE_SAMPLE);
    }

    public void testDeleteAllScopes() throws TypeException
    {
        assertEquals(0, configurationTemplateManager.deleteAll(PathUtils.WILDCARD_ANY_ELEMENT));
        assertTrue(configurationTemplateManager.pathExists(SCOPE_SAMPLE));
        assertTrue(configurationTemplateManager.pathExists(SCOPE_TEMPLATED));
    }

    public void testDeleteAllPermanentItem()
    {
        MockA tempA = new MockA("tempA");
        MockA permA = new MockA("permA");
        permA.setPermanent(true);
        String path1 = configurationTemplateManager.insert(SCOPE_SAMPLE, tempA);
        String path2 = configurationTemplateManager.insert(SCOPE_SAMPLE, permA);

        assertEquals(1, configurationTemplateManager.deleteAll("sample/*"));

        assertNoSuchPath(path1);
        assertTrue(configurationTemplateManager.pathExists(path2));
    }

    public void testDeleteAllInheritedComposite() throws TypeException
    {
        String path = PathUtils.getPath(insertInherited(), "mock");
        assertEquals(0, configurationTemplateManager.deleteAll(path));
        assertTrue(configurationTemplateManager.pathExists(path));
    }

    public void testDeleteListItem()
    {
        MockA a = new MockA("a");
        MockD d1 = new MockD("d1");
        MockD d2 = new MockD("d2");
        a.getDs().add(d1);
        a.getDs().add(d2);

        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals(2, a.getDs().size());
        assertEquals("d1", a.getDs().get(0).getName());
        assertEquals("d2", a.getDs().get(1).getName());

        configurationTemplateManager.delete(a.getDs().get(0).getConfigurationPath());
        a = configurationTemplateManager.getInstance(path, MockA.class);
        assertEquals(1, a.getDs().size());
        assertEquals("d2", a.getDs().get(0).getName());

        Record aRecord = configurationTemplateManager.getRecord(path);
        Record dsRecord = (Record) aRecord.get("ds");
        assertEquals(1, dsRecord.size());
        Record d2Record = (Record) dsRecord.get(dsRecord.keySet().iterator().next());
        assertEquals("d2", d2Record.get("name"));
    }

    public void testDeleteListItemFromTemplateChild() throws TypeException
    {
        MockA parent = new MockA("parent");
        MockA child = new MockA("child");
        MockD parentD = new MockD("pd");
        MockD childD = new MockD("cd");
        parent.getDs().add(parentD);
        child.getDs().add(childD);

        MutableRecord parentRecord = typeA.unstantiate(parent);
        configurationTemplateManager.markAsTemplate(parentRecord);
        String parentPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, parentRecord);

        Record loadedParent = configurationTemplateManager.getRecord(parentPath);

        MutableRecord childRecord = typeA.unstantiate(child);
        configurationTemplateManager.setParentTemplate(childRecord, loadedParent.getHandle());
        String childPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, childRecord);

        child = configurationTemplateManager.getInstance(childPath, MockA.class);
        assertEquals(2, child.getDs().size());
        assertEquals("pd", child.getDs().get(0).getName());
        assertEquals("cd", child.getDs().get(1).getName());

        configurationTemplateManager.delete(child.getDs().get(1).getConfigurationPath());
        child = configurationTemplateManager.getInstance(childPath, MockA.class);
        assertEquals(1, child.getDs().size());
        assertEquals("pd", child.getDs().get(0).getName());

        Record loadedChild = configurationTemplateManager.getRecord(childPath);
        Record dsRecord = (Record) loadedChild.get("ds");
        assertEquals(1, dsRecord.size());
        Record d2Record = (Record) dsRecord.get(dsRecord.keySet().iterator().next());
        assertEquals("pd", d2Record.get("name"));
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
        assertTrue(configurationTemplateManager.pathExists(SCOPE_SAMPLE));
    }

    public void testPathExistsExistantPath()
    {
        MockA a = new MockA("mock");
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertTrue(configurationTemplateManager.pathExists(path));
    }

    public void testEventGeneratedOnSave()
    {
        MockA a = new MockA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        RecordingEventListener listener = new RecordingEventListener();
        eventManager.register(listener);

        assertEquals(0, listener.getEvents().size());

        MockA instance = (MockA) configurationTemplateManager.getInstance("sample/a");
        instance.setB("B");

        configurationTemplateManager.save(instance);

        assertEquals(2, listener.getEvents().size());
        Event evt = listener.getEvents().get(0);
        assertTrue(evt instanceof SaveEvent);
        evt = listener.getEvents().get(1);
        assertTrue(evt instanceof PostSaveEvent);
    }

    public void testEventsGeneratedOnInsert()
    {
        RecordingEventListener listener = new RecordingEventListener();
        eventManager.register(listener);

        MockA a = new MockA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        assertEquals(2, listener.getEvents().size());
        Event evt = listener.getEvents().get(0);
        assertTrue(evt instanceof InsertEvent);
        evt = listener.getEvents().get(1);
        assertTrue(evt instanceof PostInsertEvent);
    }

    public void testEventsGeneratedOnDelete()
    {
        MockA a = new MockA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        RecordingEventListener listener = new RecordingEventListener();
        eventManager.register(listener);

        assertEquals(0, listener.getEvents().size());

        configurationTemplateManager.delete("sample/a");

        assertEquals(2, listener.getEvents().size());
        Event evt = listener.getEvents().get(0);
        assertTrue(evt instanceof DeleteEvent);
        evt = listener.getEvents().get(1);
        assertTrue(evt instanceof PostDeleteEvent);
    }

    public void testEventsArePublishedOnPostCommit()
    {
        UserTransaction transaction = new UserTransaction(transactionManager);
        transaction.begin();

        RecordingEventListener listener = new RecordingEventListener();
        eventManager.register(listener);

        MockA a = new MockA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        assertEquals(1, listener.getEvents().size());

        transaction.commit();

        assertEquals(2, listener.getEvents().size());
    }

    public void testEventsAreNotPublishedOnPostRollback()
    {
        UserTransaction transaction = new UserTransaction(transactionManager);
        transaction.begin();

        RecordingEventListener listener = new RecordingEventListener();
        eventManager.register(listener);

        MockA a = new MockA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        assertEquals(1, listener.getEvents().size());

        transaction.rollback();

        assertEquals(1, listener.getEvents().size());
    }

    public void testInstanceCacheAwareOfRollback()
    {
        UserTransaction transaction = new UserTransaction(transactionManager);
        transaction.begin();

        MockA a = new MockA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        assertNotNull(configurationTemplateManager.getInstance("sample/a"));

        transaction.rollback();

        assertNull(configurationTemplateManager.getInstance("sample/a"));
    }

    public void testInstanceCacheThreadIsolation()
    {
        UserTransaction transaction = new UserTransaction(transactionManager);
        transaction.begin();

        MockA a = new MockA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        assertNotNull(configurationTemplateManager.getInstance("sample/a"));

        executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                assertNull(configurationTemplateManager.getInstance("sample/a"));
            }
        });

        transaction.commit();

        executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                assertNotNull(configurationTemplateManager.getInstance("sample/a"));
            }
        });
    }

    public void testInTemplateParentInvalidPath()
    {
        assertFalse(configurationTemplateManager.existsInTemplateParent("there/is/no/such/path"));
    }

    public void testInTemplateParentEmptyPath()
    {
        assertFalse(configurationTemplateManager.existsInTemplateParent(""));
    }

    public void testInTemplateParentScope()
    {
        assertFalse(configurationTemplateManager.existsInTemplateParent(SCOPE_SAMPLE));
    }

    public void testInTemplateParentTemplatedScope()
    {
        assertFalse(configurationTemplateManager.existsInTemplateParent(SCOPE_TEMPLATED));
    }

    public void testInTemplateParentRegularRecord()
    {
        MockA a = new MockA("a");
        a.setMock(new MockB("b"));
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertFalse(configurationTemplateManager.existsInTemplateParent(PathUtils.getPath(path, "mock")));
    }

    public void testInTemplateParentTemplateRecord()
    {
        MockA a = new MockA("a");
        a.setMock(new MockB("b"));
        String path = configurationTemplateManager.insert(SCOPE_TEMPLATED, a);
        assertFalse(configurationTemplateManager.existsInTemplateParent(PathUtils.getPath(path, "mock")));
    }

    public void testInTemplateParentInheritedComposite() throws TypeException
    {
        MockA parent = new MockA("parent");
        parent.setMock(new MockB("inheritme"));
        MockA child = new MockA("child");
        String childPath = insertParentAndChildA(parent, child).second;
        assertTrue(configurationTemplateManager.existsInTemplateParent(PathUtils.getPath(childPath, "mock")));
    }

    public void testInTemplateParentOwnedComposite() throws TypeException
    {
        MockA parent = new MockA("parent");
        MockA child = new MockA("child");
        child.setMock(new MockB("ownme"));
        String childPath = insertParentAndChildA(parent, child).second;
        assertFalse(configurationTemplateManager.existsInTemplateParent(PathUtils.getPath(childPath, "mock")));
    }

    public void testInTemplateParentOverriddenComposite() throws TypeException
    {
        MockA parent = new MockA("parent");
        parent.setMock(new MockB("overrideme"));
        MockA child = new MockA("child");
        MockB b = new MockB("overrideme");
        b.setB("hehe");
        child.setMock(b);
        String childPath = insertParentAndChildA(parent, child).second;
        assertTrue(configurationTemplateManager.existsInTemplateParent(PathUtils.getPath(childPath, "mock")));
    }

    public void testInTemplateParentInheritedValue() throws TypeException
    {
        MockA parent = new MockA("parent");
        parent.setB("pb");
        MockA child = new MockA("child");
        String childPath = insertParentAndChildA(parent, child).second;
        assertTrue(configurationTemplateManager.existsInTemplateParent(PathUtils.getPath(childPath, "b")));
    }

    public void testInTemplateParentOwnedValue() throws TypeException
    {
        MockA parent = new MockA("parent");
        parent.setB("pb");
        MockA child = new MockA("child");
        child.setB("cb");
        String childPath = insertParentAndChildA(parent, child).second;
        assertTrue(configurationTemplateManager.existsInTemplateParent(PathUtils.getPath(childPath, "b")));
    }

    public void testIsOverriddenInvalidPath()
    {
        assertFalse(configurationTemplateManager.isOverridden("there/is/no/such/path"));
    }

    public void testIsOverriddenEmptyPath()
    {
        assertFalse(configurationTemplateManager.isOverridden(""));
    }

    public void testIsOverriddenScope()
    {
        assertFalse(configurationTemplateManager.isOverridden(SCOPE_SAMPLE));
    }

    public void testIsOverriddenTemplatedScope()
    {
        assertFalse(configurationTemplateManager.isOverridden(SCOPE_TEMPLATED));
    }

    public void testIsOverriddenRegularRecord()
    {
        MockA a = new MockA("a");
        a.setMock(new MockB("b"));
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(path, "mock")));
    }

    public void testIsOverriddenTemplateRecord()
    {
        MockA a = new MockA("a");
        a.setMock(new MockB("b"));
        String path = configurationTemplateManager.insert(SCOPE_TEMPLATED, a);
        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(path, "mock")));
    }

    public void testIsOverriddenInheritedComposite() throws TypeException
    {
        MockA parent = new MockA("parent");
        parent.setMock(new MockB("inheritme"));
        MockA child = new MockA("child");
        Pair<String, String> paths = insertParentAndChildA(parent, child);
        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(paths.first, "mock")));
        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(paths.second, "mock")));
    }

    public void testIsOverriddenOwnedComposite() throws TypeException
    {
        MockA parent = new MockA("parent");
        MockA child = new MockA("child");
        child.setMock(new MockB("ownme"));
        Pair<String, String> paths = insertParentAndChildA(parent, child);
        assertTrue(configurationTemplateManager.isOverridden(PathUtils.getPath(paths.first, "mock")));
        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(paths.second, "mock")));
    }

    public void testIsOverriddenOverriddenComposite() throws TypeException
    {
        MockA parent = new MockA("parent");
        parent.setMock(new MockB("overrideme"));
        MockA child = new MockA("child");
        MockB b = new MockB("overrideme");
        b.setB("hehe");
        child.setMock(b);
        Pair<String, String> paths = insertParentAndChildA(parent, child);
        assertTrue(configurationTemplateManager.isOverridden(PathUtils.getPath(paths.first, "mock")));
        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(paths.second, "mock")));
    }

    public void testIsOverriddenInheritedValue() throws TypeException
    {
        MockA parent = new MockA("parent");
        parent.setB("pb");
        MockA child = new MockA("child");
        Pair<String, String> paths = insertParentAndChildA(parent, child);
        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(paths.first, "b")));
        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(paths.second, "b")));
    }

    public void testIsOverriddenOverriddenValue() throws TypeException
    {
        MockA parent = new MockA("parent");
        parent.setB("pb");
        MockA child = new MockA("child");
        child.setB("cb");
        Pair<String, String> paths = insertParentAndChildA(parent, child);
        assertTrue(configurationTemplateManager.isOverridden(PathUtils.getPath(paths.first, "b")));
        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(paths.second, "b")));
    }

    public void testIsOverriddenOverriddenInGrandchild() throws TypeException
    {
        MockA parent = new MockA("parent");
        parent.setB("pb");
        MockA child = new MockA("child");
        MockA grandchild = new MockA("grandchild");
        grandchild.setB("cb");
        String[] paths = insertParentChildAndGrandchildA(parent, child, grandchild);
        assertTrue(configurationTemplateManager.isOverridden(PathUtils.getPath(paths[0], "b")));
        assertTrue(configurationTemplateManager.isOverridden(PathUtils.getPath(paths[1], "b")));
        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(paths[2], "b")));
    }

    public void testIsOverriddenCompositeOverriddenInGrandchild() throws TypeException
    {
        MockA parent = new MockA("parent");
        parent.setMock(new MockB("overrideme"));
        MockA child = new MockA("child");
        MockA grandchild = new MockA("grandchild");
        MockB b = new MockB("overrideme");
        b.setB("hehe");
        grandchild.setMock(b);
        String[] paths = insertParentChildAndGrandchildA(parent, child, grandchild);
        assertTrue(configurationTemplateManager.isOverridden(PathUtils.getPath(paths[0], "mock")));
        assertTrue(configurationTemplateManager.isOverridden(PathUtils.getPath(paths[1], "mock")));
        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(paths[2], "mock")));
    }

    public void testIsOverriddenCompositeHiddenInChild() throws TypeException
    {
        MockA parent = new MockA("parent");
        parent.getCs().put("hideme", new MockC("hideme"));
        MockA child = new MockA("child");
        MockA grandchild = new MockA("grandchild");
        String[] paths = insertParentChildAndGrandchildA(parent, child, grandchild);

        configurationTemplateManager.delete(PathUtils.getPath(paths[1], "cs", "hideme"));

        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(paths[0], "cs", "hideme")));
        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(paths[1], "cs", "hideme")));
        assertFalse(configurationTemplateManager.isOverridden(PathUtils.getPath(paths[2], "cs", "hideme")));
    }

    public void testAttemptToChangeReadOnlyFieldRejected() throws TypeException
    {
        CompositeType type = typeRegistry.register(ReadOnlyFieldA.class);

        configurationPersistenceManager.register("readOnlyFields", new MapType(type, typeRegistry));

        ReadOnlyFieldA a = new ReadOnlyFieldA("id", "A");

        String path = configurationTemplateManager.insert("readOnlyFields", a);
        a = (ReadOnlyFieldA) configurationTemplateManager.getInstance(path);
        a.setA("B");

        try
        {
            configurationTemplateManager.save(a);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Attempt to change readOnly property 'a' from 'A' to 'B' is not allowed.", e.getMessage());
        }
    }

    public void testConfigWithReadOnlyFieldCanBePersistedIfNoChangeOccurs() throws TypeException
    {
        CompositeType type = typeRegistry.register(ReadOnlyFieldA.class);

        configurationPersistenceManager.register("readOnlyFields", new MapType(type, typeRegistry));

        ReadOnlyFieldA a = new ReadOnlyFieldA("id", "A");

        String path = configurationTemplateManager.insert("readOnlyFields", a);
        a = (ReadOnlyFieldA) configurationTemplateManager.getInstance(path);
        a.setA("A");

        configurationTemplateManager.save(a);
    }

    public void testIsDeeplyCompleteDirectInstanceComplete()
    {
        MockB b = new MockB();
        b.setB("set");
        assertTrue(configurationTemplateManager.isDeeplyCompleteAndValid(b));
    }

    public void testIsDeeplyCompleteDirectInstanceIncomplete()
    {
        assertFalse(configurationTemplateManager.isDeeplyCompleteAndValid(new MockB()));
    }

    public void testIsDeeplyCompleteDirectInstanceInvalid()
    {
        MockB b = new MockB();
        b.setB("set");
        b.addFieldError("b", "nasty");
        assertFalse(configurationTemplateManager.isDeeplyCompleteAndValid(new MockB()));
    }

    public void testIsDeeplyCompleteIndirectInstanceComplete()
    {
        MockB b = new MockB();
        b.setB("set");
        MockA a = new MockA("a");
        a.setMock(b);

        assertTrue(configurationTemplateManager.isDeeplyCompleteAndValid(a));
    }

    public void testIsDeeplyCompleteIndirectInstanceIncomplete()
    {
        MockA a = new MockA("a");
        a.setMock(new MockB());

        assertFalse(configurationTemplateManager.isDeeplyCompleteAndValid(a));
    }

    public void testIsDeeplyCompleteIndirectInstanceInvalid()
    {
        MockB b = new MockB();
        b.setB("set");
        b.addFieldError("b", "nasty");
        MockA a = new MockA("a");
        a.setMock(b);

        assertFalse(configurationTemplateManager.isDeeplyCompleteAndValid(a));
    }

    public void testIsDeeplyCompleteIndirectInstanceNull()
    {
        assertTrue(configurationTemplateManager.isDeeplyCompleteAndValid(new MockA("a")));
    }

    public void testGetRootInstanceNonTemplated()
    {
        try
        {
            configurationTemplateManager.getRootInstance(SCOPE_SAMPLE, MockA.class);
            fail("Can't get root of non-templated scope");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Path 'sample' does not refer to a templated collection", e.getMessage());
        }
    }

    public void testGetRootInstanceTemplatedEmpty() throws TypeException
    {
        assertNull(configurationTemplateManager.getRootInstance(SCOPE_TEMPLATED, MockA.class));
    }

    public void testGetRootInstanceTemplated() throws TypeException
    {
        insertParentAndChildA(new MockA("parent"), new MockA("child"));
        assertEquals("parent", configurationTemplateManager.getRootInstance(SCOPE_TEMPLATED, MockA.class).getName());
    }

    public void testCacheInsertUpdatesParent()
    {
        // Inserting a new collection entry should refresh the parent instance.
        Configuration collection = configurationTemplateManager.getInstance(SCOPE_SAMPLE);
        configurationTemplateManager.insert(SCOPE_SAMPLE, new MockA("bar"));
        assertNotSame(collection, configurationTemplateManager.getInstance(SCOPE_SAMPLE));
    }

    public void testCacheUpdateUpdatesParent()
    {
        // Updating a collection entry should refresh the parent instance.
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, new MockA("foo"));
        Configuration collection = configurationTemplateManager.getInstance(SCOPE_SAMPLE);

        MockA item = configurationTemplateManager.getInstance(path, MockA.class);
        item = configurationTemplateManager.deepClone(item);
        item.setB("hooray");
        configurationTemplateManager.save(item);

        assertNotSame(collection, configurationTemplateManager.getInstance(SCOPE_SAMPLE));
    }

    public void testCacheDeleteUpdatesParent()
    {
        // Deleting a collection entry should refresh the parent instance.
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, new MockA("foo"));
        Configuration collection = configurationTemplateManager.getInstance(SCOPE_SAMPLE);

        configurationTemplateManager.delete(path);

        assertNotSame(collection, configurationTemplateManager.getInstance(SCOPE_SAMPLE));
    }

    public void testCacheInsertLeavesSibling()
    {
        // Inserting a new collection entry should not cause existing items in
        // that collection to refresh.
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, new MockA("foo"));
        MockA sibling = configurationTemplateManager.getInstance(path, MockA.class);

        configurationTemplateManager.insert(SCOPE_SAMPLE, new MockA("bar"));
        assertSame(sibling, configurationTemplateManager.getInstance(path, MockA.class));
    }

    public void testCacheUpdateLeavesSibling()
    {
        // Updating a collection entry should not cause other items in that
        // collection to refresh.
        String unchangedPath = configurationTemplateManager.insert(SCOPE_SAMPLE, new MockA("foo"));
        String changedPath = configurationTemplateManager.insert(SCOPE_SAMPLE, new MockA("bar"));
        MockA unchangedInstance = configurationTemplateManager.getInstance(unchangedPath, MockA.class);

        MockA item = configurationTemplateManager.getInstance(changedPath, MockA.class);
        item = configurationTemplateManager.deepClone(item);
        item.setB("hooray");
        configurationTemplateManager.save(item);

        assertSame(unchangedInstance, configurationTemplateManager.getInstance(unchangedPath, MockA.class));
    }

    public void testCacheDeleteLeavesSibling()
    {
        // Deleting a collection entry should not cause other items in that
        // collection to refresh.
        String unchangedPath = configurationTemplateManager.insert(SCOPE_SAMPLE, new MockA("foo"));
        String deletedPath = configurationTemplateManager.insert(SCOPE_SAMPLE, new MockA("bar"));
        MockA unchangedInstance = configurationTemplateManager.getInstance(unchangedPath, MockA.class);

        configurationTemplateManager.delete(deletedPath);

        assertSame(unchangedInstance, configurationTemplateManager.getInstance(unchangedPath, MockA.class));
    }

    public void testCacheUpdateUpdatesReferers()
    {
        MockReferee ee = new MockReferee("ee");
        String refereePath = configurationTemplateManager.insert(SCOPE_REFEREE, ee);
        ee = configurationTemplateManager.getInstance(refereePath, MockReferee.class);

        MockReferer er = new MockReferer("er");
        er.setRefToRef(ee);

        String refererPath = configurationTemplateManager.insert(SCOPE_REFERER, er);
        updateRefereeAndCheck(refereePath, refererPath);
    }

    public void testCacheUpdateUpdatesReferersCollection()
    {
        MockReferee ee = new MockReferee("ee");
        String refereePath = configurationTemplateManager.insert(SCOPE_REFEREE, ee);
        ee = configurationTemplateManager.getInstance(refereePath, MockReferee.class);

        MockReferer er = new MockReferer("er");
        er.getRefToRefs().add(ee);

        String refererPath = configurationTemplateManager.insert(SCOPE_REFERER, er);
        updateRefereeAndCheck(refereePath, refererPath);
    }

    private void updateRefereeAndCheck(String refereePath, String refererPath)
    {
        Configuration erCollection = configurationTemplateManager.getInstance(SCOPE_REFERER);
        MockReferer er = configurationTemplateManager.getInstance(refererPath, MockReferer.class);

        MockReferee ee = configurationTemplateManager.getInstance(refereePath, MockReferee.class);
        ee = configurationTemplateManager.deepClone(ee);
        ee.setName("new");
        configurationTemplateManager.save(ee);

        assertNotSame(er, configurationTemplateManager.getInstance(refererPath, MockReferer.class));
        assertNotSame(erCollection, configurationTemplateManager.getInstance(SCOPE_REFERER));
    }

    public void testCacheDeleteUpdatesReferers()
    {
        MockReferee ee = new MockReferee("ee");
        String refereePath = configurationTemplateManager.insert(SCOPE_REFEREE, ee);
        ee = configurationTemplateManager.getInstance(refereePath, MockReferee.class);

        MockReferer er = new MockReferer("er");
        er.setRefToRef(ee);

        String refererPath = configurationTemplateManager.insert(SCOPE_REFERER, er);
        Configuration erCollection = configurationTemplateManager.getInstance(SCOPE_REFERER);
        er = configurationTemplateManager.getInstance(refererPath, MockReferer.class);

        configurationTemplateManager.delete(refereePath);
        
        assertNotSame(er, configurationTemplateManager.getInstance(refererPath, MockReferer.class));
        assertNotSame(erCollection, configurationTemplateManager.getInstance(SCOPE_REFERER));
    }

    public void testCacheUpdateUpdatesDescendents() throws TypeException
    {
        Pair<String, String> paths = insertParentAndChildA(new MockA("parent"), new MockA("child"));
        MockA child = configurationTemplateManager.getInstance(paths.second, MockA.class);

        MockA parent = configurationTemplateManager.getInstance(paths.first, MockA.class);
        parent = configurationTemplateManager.deepClone(parent);
        parent.setB("i haz change it");
        configurationTemplateManager.save(parent);
        
        assertNotSame(child, configurationTemplateManager.getInstance(paths.second, MockA.class));
    }

    public void testCacheUpdateLeavesAncestors() throws TypeException
    {
        Pair<String, String> paths = insertParentAndChildA(new MockA("parent"), new MockA("child"));
        MockA parent = configurationTemplateManager.getInstance(paths.first, MockA.class);

        MockA child = configurationTemplateManager.getInstance(paths.second, MockA.class);
        child = configurationTemplateManager.deepClone(child);
        child.setB("i haz change it");
        configurationTemplateManager.save(child);
        
        assertSame(parent, configurationTemplateManager.getInstance(paths.first, MockA.class));
    }

    public void testCacheNestedUpdateWithDescendents() throws TypeException
    {
        MockA parent = new MockA("parent");
        parent.getCs().put("c1", new MockC("c1"));
        parent.getCs().put("c2", new MockC("c2"));
        Pair<String, String> paths = insertParentAndChildA(parent, new MockA("child"));
        MockA child = configurationTemplateManager.getInstance(paths.second, MockA.class);

        parent = configurationTemplateManager.getInstance(paths.first, MockA.class);
        parent = configurationTemplateManager.deepClone(parent);
        parent.getCs().get("c1").setD(new MockD("new"));
        configurationTemplateManager.save(parent);

        MockA newChild = configurationTemplateManager.getInstance(paths.second, MockA.class);
        assertNotSame(child, newChild);
        assertNotSame(child.getCs(), newChild.getCs());
        assertNotSame(child.getCs().get("c1"), newChild.getCs().get("c1"));
        assertSame(child.getCs().get("c2"), newChild.getCs().get("c2"));
    }

    public void testGetHighestNoneSatisfy() throws TypeException
    {
        insertParentChildAndGrandchildA(new MockA(GRANDPARENT_NAME), new MockA(CHILD_NAME), new MockA(GRANDCHILD_NAME));
        assertEquals(0, configurationTemplateManager.getHighestInstancesSatisfying(new FalsePredicate<MockA>(), MockA.class).size());
    }

    public void testGetHighestAllSatisfy() throws TypeException
    {
        insertParentChildAndGrandchildA(new MockA(GRANDPARENT_NAME), new MockA(CHILD_NAME), new MockA(GRANDCHILD_NAME));
        List<MockA> results = configurationTemplateManager.getHighestInstancesSatisfying(new TruePredicate<MockA>(), MockA.class);
        assertEquals(1, results.size());
        assertEquals(GRANDPARENT_NAME, results.get(0).getName());
    }

    public void testGetHighestGranchildSatisfies() throws TypeException
    {
        insertParentChildAndGrandchildA(new MockA(GRANDPARENT_NAME), new MockA(CHILD_NAME), new MockA(GRANDCHILD_NAME));
        List<MockA> results = configurationTemplateManager.getHighestInstancesSatisfying(new Predicate<MockA>()
        {
            public boolean satisfied(MockA mockA)
            {
                return mockA.getName().equals(GRANDCHILD_NAME);
            }
        }, MockA.class);
        
        assertEquals(1, results.size());
        assertEquals(GRANDCHILD_NAME, results.get(0).getName());
    }

    public void testGetHighestChildSatisfies() throws TypeException
    {
        insertParentChildAndGrandchildA(new MockA(GRANDPARENT_NAME), new MockA(CHILD_NAME), new MockA(GRANDCHILD_NAME));
        List<MockA> results = configurationTemplateManager.getHighestInstancesSatisfying(new Predicate<MockA>()
        {
            public boolean satisfied(MockA mockA)
            {
                return mockA.getName().equals(CHILD_NAME);
            }
        }, MockA.class);

        assertEquals(1, results.size());
        assertEquals(CHILD_NAME, results.get(0).getName());
    }

    public void testGetHighestChildAndGrandchildSatisfies() throws TypeException
    {
        insertParentChildAndGrandchildA(new MockA(GRANDPARENT_NAME), new MockA(CHILD_NAME), new MockA(GRANDCHILD_NAME));
        List<MockA> results = configurationTemplateManager.getHighestInstancesSatisfying(new Predicate<MockA>()
        {
            public boolean satisfied(MockA mockA)
            {
                return !mockA.getName().equals(GRANDPARENT_NAME);
            }
        }, MockA.class);

        assertEquals(1, results.size());
        assertEquals(CHILD_NAME, results.get(0).getName());
    }

    private Pair<String, String> insertParentAndChildA(MockA parent, MockA child) throws TypeException
    {
        MutableRecord record = typeA.unstantiate(parent);
        configurationTemplateManager.markAsTemplate(record);
        String parentPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);
        long parentHandle = recordManager.select(parentPath).getHandle();

        record = typeA.unstantiate(child);
        configurationTemplateManager.setParentTemplate(record, parentHandle);
        return new Pair<String, String>(parentPath, configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record));
    }

    private String[] insertParentChildAndGrandchildA(MockA parent, MockA child, MockA grandchild) throws TypeException
    {
        MutableRecord record = typeA.unstantiate(parent);
        configurationTemplateManager.markAsTemplate(record);
        String parentPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);
        long parentHandle = recordManager.select(parentPath).getHandle();

        record = typeA.unstantiate(child);
        configurationTemplateManager.markAsTemplate(record);
        configurationTemplateManager.setParentTemplate(record, parentHandle);
        String childPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);
        long childHandle = recordManager.select(childPath).getHandle();

        record = typeA.unstantiate(grandchild);
        configurationTemplateManager.setParentTemplate(record, childHandle);
        String grandChildPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);
        return new String[]{parentPath, childPath, grandChildPath};
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

    @SymbolicName("readOnlyFieldA")
    public static class ReadOnlyFieldA extends AbstractConfiguration
    {
        @ReadOnly
        private String a;

        @ID
        private String id;

        public ReadOnlyFieldA()
        {
        }

        public ReadOnlyFieldA(String id, String a)
        {
            this.a = a;
            this.id = id;
        }

        public void setA(String a)
        {
            this.a = a;
        }

        public String getA()
        {
            return a;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public String getId()
        {
            return id;
        }
    }

    private class RecordingEventListener implements EventListener
    {
        private List<Event> events = new LinkedList<Event>();

        public void handleEvent(Event evt)
        {
            events.add(evt);
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{ConfigurationEvent.class};
        }

        public List<Event> getEvents()
        {
            return events;
        }
    }
}
