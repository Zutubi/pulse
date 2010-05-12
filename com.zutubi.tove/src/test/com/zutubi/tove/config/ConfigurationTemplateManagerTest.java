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

import static com.zutubi.tove.type.record.PathUtils.getPath;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

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

        typeA = typeRegistry.register(ConfigA.class);
        typeB = typeRegistry.getType(ConfigB.class);
        MapType mapA = new MapType(typeA, typeRegistry);

        MapType templatedMap = new TemplatedMapType(typeA, typeRegistry);

        CompositeType typeReferer = typeRegistry.register(Referer.class);
        CompositeType typeReferee = typeRegistry.getType(Referee.class);
        MapType mapReferer = new MapType(typeReferer, typeRegistry);
        MapType mapReferee = new MapType(typeReferee, typeRegistry);

        configurationPersistenceManager.register(SCOPE_SAMPLE, mapA);
        configurationPersistenceManager.register(SCOPE_TEMPLATED, templatedMap);
        configurationPersistenceManager.register(SCOPE_REFERER, mapReferer);
        configurationPersistenceManager.register(SCOPE_REFEREE, mapReferee);

        accessManager.registerAuthorityProvider(ConfigA.class, new AuthorityProvider<ConfigA>()
        {
            public Set<String> getAllowedAuthorities(String action, ConfigA resource)
            {
                return new HashSet<String>(asList(action));
            }
        });
    }

    public void testInsertIntoCollection()
    {
        ConfigA a = new ConfigA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        ConfigA loaded = (ConfigA) configurationTemplateManager.getInstance("sample/a");
        assertNotNull(loaded);
        assertEquals("a", loaded.getName());
        assertEquals(null, loaded.getB());
    }

    public void testInsertIntoObject()
    {
        ConfigA a = new ConfigA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        ConfigB b = new ConfigB("b");
        configurationTemplateManager.insert("sample/a/composite", b);

        ConfigB loaded = (ConfigB) configurationTemplateManager.getInstance("sample/a/composite");
        assertNotNull(loaded);
        assertEquals("b", loaded.getB());
    }

    public void testInsertExistingPath()
    {
        ConfigA a = new ConfigA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        ConfigB b = new ConfigB("b");
        configurationTemplateManager.insert("sample/a/composite", b);

        try
        {
            configurationTemplateManager.insert("sample/a/composite", b);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Invalid insertion path 'sample/a/composite': record already exists (use save to modify)", e.getMessage());
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
            configurationTemplateManager.insert(SCOPE_SAMPLE, new ConfigA("a"));
            fail();
        }
        catch (Exception e)
        {
            assertEquals("Permission to create at path 'sample' denied", e.getMessage());
        }
    }

    public void testInsertTemplatedRoot()
    {
        String path = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, new ConfigA("root"), null, true);
        assertEquals(getPath(SCOPE_TEMPLATED, "root"), path);

        Record record = configurationTemplateManager.getRecord(path);
        assertNotNull(record);
        assertTrue(configurationTemplateManager.isTemplate(record));
        assertEquals("configA", record.getSymbolicName());
        assertEquals("root", record.get("name"));
    }

    public void testInsertTemplatedConcreteChild()
    {
        ConfigA root = new ConfigA("root");
        root.setB("b");
        root.setC("c");
        root.getCs().put("c1", new ConfigC("c1"));

        String rootPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, root, null, true);

        ConfigA child = new ConfigA("child");
        child.setB("b");
        child.setC("override");

        String childPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, child, rootPath, false);

        assertEquals(getPath(SCOPE_TEMPLATED, "child"), childPath);

        Record record = configurationTemplateManager.getRecord(childPath);
        assertNotNull(record);
        assertFalse(configurationTemplateManager.isTemplate(record));
        assertEquals("child", record.get("name"));
        assertEquals("b", record.get("b"));
        assertEquals("override", record.get("c"));

        Record collectionRecord = (Record) record.get("cs");
        Record itemRecord = (Record) collectionRecord.get("c1");
        assertNotNull(itemRecord);
        assertEquals("c1", itemRecord.get("name"));
    }

    public void testInsertTemplatedInvalidScope()
    {
        try
        {
            configurationTemplateManager.insertTemplated("invalid", new ConfigA(), null, true);
            fail("Should not be able to insert into invalid scope");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("Scope 'invalid' is invalid"));
        }
    }

    public void testInsertTemplatedNonTemplatedScope()
    {
        try
        {
            configurationTemplateManager.insertTemplated(SCOPE_SAMPLE, new ConfigA(), null, true);
            fail("Should not be able to insert into a non-templated scope");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("is not templated"));
        }
    }

    public void testInsertTemplatedBadType()
    {
        try
        {
            configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, new ConfigB(), null, true);
            fail("Should not be able to insert an item of the wrong type");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("Instance type 'configB' does not match scope item type 'configA'"));
        }
    }

    public void testInsertTemplatedInvalidTemplateParentPath()
    {
        try
        {
            configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, new ConfigA("joe"), getPath(SCOPE_TEMPLATED, "invalid"), true);
            fail("Should not be able to insert an item with an invalid parent");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("Template parent path 'template/invalid' is invalid"));
        }
    }

    public void testInsertTemplatedTemplateParentNotItemOfSameCollection()
    {
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, new ConfigA("bad parental"));
        try
        {
            configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, new ConfigA("joe"), path, true);
            fail("Should not be able to insert an item with a bad parent");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("does not refer to an element of the same templated collection"));
        }
    }

    public void testInsertTemplatedTemplateParentConcrete()
    {
        String rootPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, new ConfigA("root"), null, true);
        String concretePath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, new ConfigA("conker"), rootPath, false);
        try
        {
            configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, new ConfigA("joe"), concretePath, false);
            fail("Should not be able to insert an item with a concrete parent");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("refers to a concrete record"));
        }
    }

    public void testInsertTemplatedNoParentNotTemplate()
    {
        try
        {
            configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, new ConfigA("root"), null, false);
            fail("Should not be able to insert a concrete item as the scope root");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("Inserted item must have a parent or be a template itself"));
        }
    }

    public void testInsertTemplatedSecondRoot()
    {
        configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, new ConfigA("root"), null, true);
        try
        {
            configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, new ConfigA("twoot"), null, true);
            fail("Should not be able to insert a second root");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("already has a root item"));
        }
    }

    public void testSave()
    {
        ConfigA a = new ConfigA("a");
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertNotNull(a);
        a = configurationTemplateManager.deepClone(a);
        a.setB("somevalue");
        configurationTemplateManager.save(a);

        ConfigA loaded = (ConfigA) configurationTemplateManager.getInstance("sample/a");
        assertNotNull(loaded);
        assertEquals("a", loaded.getName());
        assertEquals("somevalue", loaded.getB());
    }

    public void testSaveIsDeep()
    {
        ConfigA a = new ConfigA("a");
        a.setComposite(new ConfigB("b"));
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertEquals("b", a.getComposite().getB());

        a = configurationTemplateManager.deepClone(a);
        a.getComposite().setB("c");
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertEquals("c", a.getComposite().getB());
    }

    public void testSaveInsertsTransitively()
    {
        ConfigA a = new ConfigA("a");
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertEquals(0, a.getCs().size());

        a = configurationTemplateManager.deepClone(a);
        ConfigD d = new ConfigD("d");
        ConfigC c = new ConfigC("c");
        c.setD(d);
        a.getCs().put("c", c);
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertEquals(1, a.getCs().size());
        c = a.getCs().get("c");
        assertNotNull(c);
        d = c.getD();
        assertNotNull(d);
        assertEquals("d", d.getName());
    }

    public void testSaveSavesTransitively()
    {
        ConfigD d = new ConfigD("d");
        ConfigC c = new ConfigC("c");
        c.setD(d);
        ConfigA a = new ConfigA("a");
        a.getCs().put("c", c);

        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertEquals(1, a.getCs().size());
        c = a.getCs().get("c");
        assertNotNull(c);
        d = c.getD();
        assertNotNull(d);
        assertEquals("d", d.getName());

        a = configurationTemplateManager.deepClone(a);
        a.getCs().get("c").getD().setName("newname");
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertEquals("newname", a.getCs().get("c").getD().getName());
    }

    public void testSaveChildObjectAdded()
    {
        ConfigA a = new ConfigA("a");
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertNull(a.getComposite());

        a = configurationTemplateManager.deepClone(a);
        a.setComposite(new ConfigB("b"));
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertNotNull(a.getComposite());
        assertEquals("b", a.getComposite().getB());
    }

    public void testSaveChildObjectDeleted()
    {
        ConfigA a = new ConfigA("a");
        a.setComposite(new ConfigB("b"));
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertNotNull(a.getComposite());
        assertEquals("b", a.getComposite().getB());

        a = configurationTemplateManager.deepClone(a);
        a.setComposite(null);
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertNull(a.getComposite());
    }

    public void testSaveCollectionElementAdded()
    {
        ConfigA a = new ConfigA("a");
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertEquals(0, a.getCs().size());

        a = configurationTemplateManager.deepClone(a);
        a.getCs().put("jim", new ConfigC("jim"));
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertEquals(1, a.getCs().size());
        assertNotNull(a.getCs().get("jim"));
    }

    public void testSaveCollectionElementRemoved()
    {
        ConfigA a = new ConfigA("a");
        a.getCs().put("jim", new ConfigC("jim"));
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertEquals(1, a.getCs().size());
        assertNotNull(a.getCs().get("jim"));

        a = configurationTemplateManager.deepClone(a);
        a.getCs().remove("jim");
        configurationTemplateManager.save(a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertEquals(0, a.getCs().size());
    }

    public void testSaveNoPermission()
    {
        configurationTemplateManager.insert(SCOPE_SAMPLE, new ConfigA("a"));

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
        ConfigA a = new ConfigA("a");
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertNotNull(a);

        // change the ID field, effectively triggering a rename on save.
        a = configurationTemplateManager.deepClone(a);
        a.setName("b");
        configurationTemplateManager.save(a);

        assertNull(configurationTemplateManager.getInstance("sample/a"));

        ConfigA loaded = (ConfigA) configurationTemplateManager.getInstance("sample/b");
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

        ConfigA a = new ConfigA("a");
        a.setComposite(new ConfigB("b"));

        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        assertEquals(4, events.size());
        assertTrue(events.get(0) instanceof InsertEvent);
        assertEquals("sample/a", events.get(0).getInstance().getConfigurationPath());
        assertTrue(events.get(1) instanceof InsertEvent);
        assertEquals("sample/a/composite", events.get(1).getInstance().getConfigurationPath());
        assertTrue(events.get(2) instanceof PostInsertEvent);
        assertEquals("sample/a", events.get(2).getInstance().getConfigurationPath());
        assertTrue(events.get(3) instanceof PostInsertEvent);
        assertEquals("sample/a/composite", events.get(3).getInstance().getConfigurationPath());
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
        ConfigA a = new ConfigA("aburger");
        ConfigB b = new ConfigB("bburger");
        a.setComposite(b);

        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = configurationTemplateManager.getInstance("sample/aburger", ConfigA.class);
        ConfigA aClone = configurationTemplateManager.deepClone(a);

        assertNotSame(a, aClone);
        assertNotSame(a.getComposite(), aClone.getComposite());
        assertEquals(a.getHandle(), aClone.getHandle());
        assertEquals(a.getConfigurationPath(), aClone.getConfigurationPath());
        assertEquals("aburger", aClone.getName());
        assertEquals("bburger", aClone.getComposite().getB());
    }

    public void testDeepCloneWithReferences()
    {
        Referee ee = new Referee("ee");
        configurationTemplateManager.insert(SCOPE_REFEREE, ee);
        ee = configurationTemplateManager.getInstance("referee/ee", Referee.class);

        Referer er = new Referer("er");
        er.setRefToRef(ee);
        er.getRefToRefs().add(ee);

        configurationTemplateManager.insert(SCOPE_REFERER, er);
        er = configurationTemplateManager.getInstance("referer/er", Referer.class);
        ee = configurationTemplateManager.getInstance("referee/ee", Referee.class);

        Referer clone = configurationTemplateManager.deepClone(er);

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
        Referee ee = new Referee("ee");
        Referer er = new Referer("er");
        er.setRef(ee);
        configurationTemplateManager.insert(SCOPE_REFERER, er);
        er = configurationTemplateManager.getInstance("referer/er", Referer.class);
        er = configurationTemplateManager.deepClone(er);
        ee = er.getRef();
        er.setRefToRef(ee);
        er.getRefToRefs().add(ee);
        configurationTemplateManager.save(er);

        er = configurationTemplateManager.getInstance("referer/er", Referer.class);
        ee = er.getRef();
        assertSame(ee, er.getRefToRef());
        assertSame(ee, er.getRefToRefs().get(0));

        // Now we can actually clone and test.
        Referer clone = configurationTemplateManager.deepClone(er);
        Referee refClone = clone.getRef();

        assertNotSame(er, clone);
        assertNotSame(ee, refClone);
        assertSame(refClone, clone.getRefToRef());
        assertSame(refClone, clone.getRefToRefs().get(0));
    }

    public void testDeepClonePreservesPaths() throws TypeException
    {
        ConfigA a = new ConfigA("a");
        a.setComposite(new ConfigB("b"));
        a.getCs().put("c", new ConfigC("c"));
        a.getDs().add(new ConfigD("d"));
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = (ConfigA) configurationTemplateManager.getInstance("sample/a");

        ConfigA clone = configurationTemplateManager.deepClone(a);
        assertEquals(a.getConfigurationPath(), clone.getConfigurationPath());
        assertEquals(a.getComposite().getConfigurationPath(), clone.getComposite().getConfigurationPath());
        assertEquals(a.getCs().get("c").getConfigurationPath(), clone.getCs().get("c").getConfigurationPath());
        assertEquals(a.getDs().get(0).getConfigurationPath(), clone.getDs().get(0).getConfigurationPath());
    }

    public void testDeepClonePreservesHandles() throws TypeException
    {
        ConfigA a = new ConfigA("a");
        a.setComposite(new ConfigB("b"));
        a.getCs().put("c", new ConfigC("c"));
        a.getDs().add(new ConfigD("d"));
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        a = (ConfigA) configurationTemplateManager.getInstance("sample/a");

        ConfigA clone = configurationTemplateManager.deepClone(a);
        assertEquals(a.getHandle(), clone.getHandle());
        assertEquals(a.getComposite().getHandle(), clone.getComposite().getHandle());
        assertEquals(a.getCs().get("c").getHandle(), clone.getCs().get("c").getHandle());
        assertEquals(a.getDs().get(0).getHandle(), clone.getDs().get(0).getHandle());
    }

    public void testDeepCloneAndSavePreservesMeta() throws TypeException
    {
        ConfigA a = new ConfigA("a");
        MutableRecord record = unstantiate(a);
        record.putMeta("testkey", "value");

        String path = configurationTemplateManager.insertRecord(SCOPE_SAMPLE, record);
        Record savedRecord = configurationTemplateManager.getRecord(path);
        assertEquals("value", savedRecord.getMeta("testkey"));

        a = configurationTemplateManager.getInstance(path, ConfigA.class);
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
        ConfigB instance = configurationTemplateManager.validate("template/a", "composite", record, false, false);
        assertTrue(instance.isValid());
    }

    public void testValidateTemplateIdStillRequired() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        configurationTemplateManager.markAsTemplate(record);
        ConfigA instance = configurationTemplateManager.validate(SCOPE_TEMPLATED, "", record, false, false);
        assertFalse(instance.isValid());
        final List<String> errors = instance.getFieldErrors("name");
        assertEquals(1, errors.size());
        assertEquals("name requires a value", errors.get(0));
    }

    public void testValidateNestedPath() throws TypeException
    {
        MutableRecord record = typeA.createNewRecord(true);
        configurationTemplateManager.markAsTemplate(record);
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
        ConfigB instance = configurationTemplateManager.validate("template/a", "b", record, false, false);
        assertTrue(instance.isValid());
        assertNull(instance.getB());
    }

    public void testCachedInstancesAreValidated() throws TypeException
    {
        ConfigA a = new ConfigA("a");
        a.setComposite(new ConfigB());
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        ConfigB instance = configurationTemplateManager.getInstance("sample/a/composite", ConfigB.class);
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

        ConfigA instance = configurationTemplateManager.getInstance("template/a$", ConfigA.class);
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

        configurationTemplateManager.insert("template/a/composite", new ConfigB());

        ConfigB instance = configurationTemplateManager.getInstance("template/a/composite", ConfigB.class);
        assertTrue(instance.isValid());
    }

    public void testValidateNotDeep() throws TypeException
    {
        ConfigA a = new ConfigA("a");
        a.setComposite(new ConfigB());
        MutableRecord record = unstantiate(a);

        ConfigA instance = configurationTemplateManager.validate(SCOPE_SAMPLE, null, record, true, false);
        assertTrue(instance.isValid());
        assertTrue(instance.getComposite().isValid());
    }

    public void testValidateDeep() throws TypeException
    {
        ConfigA a = new ConfigA("a");
        a.setComposite(new ConfigB());
        MutableRecord record = unstantiate(a);

        ConfigA instance = configurationTemplateManager.validate(SCOPE_SAMPLE, null, record, true, true);
        assertTrue(instance.isValid());
        assertFalse(instance.getComposite().isValid());
    }

    public void testValidateDeepNestedList() throws TypeException
    {
        ConfigA a = new ConfigA("a");
        a.getDs().add(new ConfigD());
        Record record = unstantiate(a);

        ConfigA validated = configurationTemplateManager.validate(SCOPE_SAMPLE, null, record, true, true);
        assertTrue(validated.isValid());
        ConfigD configD = validated.getDs().get(0);
        assertMissingName(configD);
    }

    public void testValidateDeepNestedMap() throws TypeException
    {
        ConfigA a = new ConfigA("a");
        a.getCs().put("name", new ConfigC());
        Record record = unstantiate(a);

        ConfigA validated = configurationTemplateManager.validate(SCOPE_SAMPLE, null, record, true, true);
        assertTrue(validated.isValid());
        ConfigC configC = validated.getCs().get("name");
        assertMissingName(configC);
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

        MutableRecord record = typeA.createNewRecord(true);
        configurationTemplateManager.markAsTemplate(record);
        record.put("name", "a");
        configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);

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
        ConfigA a = new ConfigA("test");
        a.setPermanent(true);
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertFalse(configurationTemplateManager.canDelete(path));
    }

    public void testCanDeleteInheritedComposite() throws TypeException
    {
        assertFalse(configurationTemplateManager.canDelete(getPath(insertInherited(), "composite")));
    }

    public void testCanDeleteSimple()
    {
        ConfigA a = new ConfigA("test");
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertTrue(configurationTemplateManager.canDelete(path));
    }

    public void testCanDeleteCompositeChild()
    {
        ConfigA a = new ConfigA("test");
        a.setComposite(new ConfigB("b"));
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertTrue(configurationTemplateManager.canDelete(getPath(path, "composite")));
    }

    public void testCanDeleteMapItem()
    {
        ConfigA a = new ConfigA("test");
        a.getCs().put("cee", new ConfigC("cee"));
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertTrue(configurationTemplateManager.canDelete(getPath(path, "cs/cee")));
    }

    public void testCanDeleteOwnedComposite() throws TypeException
    {
        insertInherited();
        assertTrue(configurationTemplateManager.canDelete("template/test/composite"));
    }

    public void testCanDeleteInheritedMapItem() throws TypeException
    {
        assertTrue(configurationTemplateManager.canDelete(getPath(insertInherited(), "cs/cee")));
    }

    private String insertInherited() throws TypeException
    {
        ConfigA a = new ConfigA("test");
        a.setComposite(new ConfigB("bee"));
        a.getCs().put("cee", new ConfigC("cee"));
        MutableRecord record = unstantiate(a);
        configurationTemplateManager.markAsTemplate(record);
        String path = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);

        record = unstantiate(new ConfigA("child"));
        configurationTemplateManager.setParentTemplate(record, recordManager.select(path).getHandle());
        path = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);
        return path;
    }

    public void testDelete()
    {
        ConfigA a = new ConfigA("test");
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
            ConfigA a = new ConfigA("test");
            a.setPermanent(true);
            String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);

            configurationTemplateManager.delete(path);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Cannot delete instance at path 'sample/test': marked permanent", e.getMessage());
        }
    }

    public void testDeleteScope() throws TypeException
    {
        failedDeleteHelper(SCOPE_SAMPLE, "Invalid path 'sample': cannot delete a scope");
    }

    public void testDeleteInheritedComposite() throws TypeException
    {
        String path = getPath(insertInherited(), "composite");
        failedDeleteHelper(path, "Invalid path 'template/child/composite': cannot delete an inherited composite property");
    }

    public void testDeleteNoPermission()
    {
        configurationTemplateManager.insert(SCOPE_SAMPLE, new ConfigA("a"));
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
        ConfigA a = new ConfigA("test");
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
        ConfigA a1 = new ConfigA("a1");
        ConfigA a2 = new ConfigA("a2");
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
        ConfigA tempA = new ConfigA("tempA");
        ConfigA permA = new ConfigA("permA");
        permA.setPermanent(true);
        String path1 = configurationTemplateManager.insert(SCOPE_SAMPLE, tempA);
        String path2 = configurationTemplateManager.insert(SCOPE_SAMPLE, permA);

        assertEquals(1, configurationTemplateManager.deleteAll("sample/*"));

        assertNoSuchPath(path1);
        assertTrue(configurationTemplateManager.pathExists(path2));
    }

    public void testDeleteAllInheritedComposite() throws TypeException
    {
        String path = getPath(insertInherited(), "composite");
        assertEquals(0, configurationTemplateManager.deleteAll(path));
        assertTrue(configurationTemplateManager.pathExists(path));
    }

    public void testDeleteListItem()
    {
        ConfigA a = new ConfigA("a");
        ConfigD d1 = new ConfigD("d1");
        ConfigD d2 = new ConfigD("d2");
        a.getDs().add(d1);
        a.getDs().add(d2);

        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        a = configurationTemplateManager.getInstance(path, ConfigA.class);
        assertEquals(2, a.getDs().size());
        assertEquals("d1", a.getDs().get(0).getName());
        assertEquals("d2", a.getDs().get(1).getName());

        configurationTemplateManager.delete(a.getDs().get(0).getConfigurationPath());
        a = configurationTemplateManager.getInstance(path, ConfigA.class);
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
        ConfigA parent = new ConfigA("parent");
        ConfigA child = new ConfigA("child");
        ConfigD parentD = new ConfigD("pd");
        ConfigD childD = new ConfigD("cd");
        parent.getDs().add(parentD);
        child.getDs().add(childD);

        MutableRecord parentRecord = unstantiate(parent);
        configurationTemplateManager.markAsTemplate(parentRecord);
        String parentPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, parentRecord);

        Record loadedParent = configurationTemplateManager.getRecord(parentPath);

        MutableRecord childRecord = unstantiate(child);
        configurationTemplateManager.setParentTemplate(childRecord, loadedParent.getHandle());
        String childPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, childRecord);

        child = configurationTemplateManager.getInstance(childPath, ConfigA.class);
        assertEquals(2, child.getDs().size());
        assertEquals("pd", child.getDs().get(0).getName());
        assertEquals("cd", child.getDs().get(1).getName());

        configurationTemplateManager.delete(child.getDs().get(1).getConfigurationPath());
        child = configurationTemplateManager.getInstance(childPath, ConfigA.class);
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
        ConfigA a = new ConfigA("test");
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertTrue(configurationTemplateManager.pathExists(path));
    }

    public void testEventGeneratedOnSave()
    {
        ConfigA a = new ConfigA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        RecordingEventListener listener = new RecordingEventListener();
        eventManager.register(listener);

        assertEquals(0, listener.getEvents().size());

        ConfigA instance = (ConfigA) configurationTemplateManager.getInstance("sample/a");
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

        ConfigA a = new ConfigA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        assertEquals(2, listener.getEvents().size());
        Event evt = listener.getEvents().get(0);
        assertTrue(evt instanceof InsertEvent);
        evt = listener.getEvents().get(1);
        assertTrue(evt instanceof PostInsertEvent);
    }

    public void testEventsGeneratedOnDelete()
    {
        ConfigA a = new ConfigA("a");
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

        ConfigA a = new ConfigA("a");
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

        ConfigA a = new ConfigA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        assertEquals(1, listener.getEvents().size());

        transaction.rollback();

        assertEquals(1, listener.getEvents().size());
    }

    public void testInstanceCacheAwareOfRollback()
    {
        UserTransaction transaction = new UserTransaction(transactionManager);
        transaction.begin();

        ConfigA a = new ConfigA("a");
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);

        assertNotNull(configurationTemplateManager.getInstance("sample/a"));

        transaction.rollback();

        assertNull(configurationTemplateManager.getInstance("sample/a"));
    }

    public void testInstanceCacheThreadIsolation()
    {
        UserTransaction transaction = new UserTransaction(transactionManager);
        transaction.begin();

        ConfigA a = new ConfigA("a");
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
        ConfigA a = new ConfigA("a");
        a.setComposite(new ConfigB("b"));
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertFalse(configurationTemplateManager.existsInTemplateParent(getPath(path, "composite")));
    }

    public void testInTemplateParentTemplateRecord()
    {
        MutableRecord record = typeA.createNewRecord(true);
        configurationTemplateManager.markAsTemplate(record);
        record.put("name", "a");
        String aPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);

        ConfigA a = configurationTemplateManager.getInstance(aPath, ConfigA.class);
        a.setComposite(new ConfigB("b"));
        String path = configurationTemplateManager.save(a);
        assertFalse(configurationTemplateManager.existsInTemplateParent(getPath(path, "composite")));
    }

    public void testInTemplateParentInheritedComposite() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setComposite(new ConfigB("inheritme"));
        ConfigA child = new ConfigA("child");
        String childPath = insertParentAndChildA(parent, child).second;
        assertTrue(configurationTemplateManager.existsInTemplateParent(getPath(childPath, "composite")));
    }

    public void testInTemplateParentOwnedComposite() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        ConfigA child = new ConfigA("child");
        child.setComposite(new ConfigB("ownme"));
        String childPath = insertParentAndChildA(parent, child).second;
        assertFalse(configurationTemplateManager.existsInTemplateParent(getPath(childPath, "composite")));
    }

    public void testInTemplateParentOverriddenComposite() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setComposite(new ConfigB("overrideme"));
        ConfigA child = new ConfigA("child");
        ConfigB b = new ConfigB("overrideme");
        b.setB("hehe");
        child.setComposite(b);
        String childPath = insertParentAndChildA(parent, child).second;
        assertTrue(configurationTemplateManager.existsInTemplateParent(getPath(childPath, "composite")));
    }

    public void testInTemplateParentInheritedValue() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setB("pb");
        ConfigA child = new ConfigA("child");
        String childPath = insertParentAndChildA(parent, child).second;
        assertTrue(configurationTemplateManager.existsInTemplateParent(getPath(childPath, "b")));
    }

    public void testInTemplateParentOwnedValue() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setB("pb");
        ConfigA child = new ConfigA("child");
        child.setB("cb");
        String childPath = insertParentAndChildA(parent, child).second;
        assertTrue(configurationTemplateManager.existsInTemplateParent(getPath(childPath, "b")));
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
        ConfigA a = new ConfigA("a");
        a.setComposite(new ConfigB("b"));
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        assertFalse(configurationTemplateManager.isOverridden(getPath(path, "composite")));
    }

    public void testIsOverriddenTemplateRecord()
    {
        MutableRecord record = typeA.createNewRecord(true);
        configurationTemplateManager.markAsTemplate(record);
        record.put("name", "a");
        String aPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);

        ConfigA a = configurationTemplateManager.getInstance(aPath, ConfigA.class);
        a.setComposite(new ConfigB("b"));
        String path = configurationTemplateManager.save(a);
        assertFalse(configurationTemplateManager.isOverridden(getPath(path, "composite")));
    }

    public void testIsOverriddenInheritedComposite() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setComposite(new ConfigB("inheritme"));
        ConfigA child = new ConfigA("child");
        Pair<String, String> paths = insertParentAndChildA(parent, child);
        assertFalse(configurationTemplateManager.isOverridden(getPath(paths.first, "composite")));
        assertFalse(configurationTemplateManager.isOverridden(getPath(paths.second, "composite")));
    }

    public void testIsOverriddenOwnedComposite() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        ConfigA child = new ConfigA("child");
        child.setComposite(new ConfigB("ownme"));
        Pair<String, String> paths = insertParentAndChildA(parent, child);
        assertTrue(configurationTemplateManager.isOverridden(getPath(paths.first, "composite")));
        assertFalse(configurationTemplateManager.isOverridden(getPath(paths.second, "composite")));
    }

    public void testIsOverriddenOverriddenComposite() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setComposite(new ConfigB("overrideme"));
        ConfigA child = new ConfigA("child");
        ConfigB b = new ConfigB("overrideme");
        b.setB("hehe");
        child.setComposite(b);
        Pair<String, String> paths = insertParentAndChildA(parent, child);
        assertTrue(configurationTemplateManager.isOverridden(getPath(paths.first, "composite")));
        assertFalse(configurationTemplateManager.isOverridden(getPath(paths.second, "composite")));
    }

    public void testIsOverriddenInheritedValue() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setB("pb");
        ConfigA child = new ConfigA("child");
        Pair<String, String> paths = insertParentAndChildA(parent, child);
        assertFalse(configurationTemplateManager.isOverridden(getPath(paths.first, "b")));
        assertFalse(configurationTemplateManager.isOverridden(getPath(paths.second, "b")));
    }

    public void testIsOverriddenOverriddenValue() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setB("pb");
        ConfigA child = new ConfigA("child");
        child.setB("cb");
        Pair<String, String> paths = insertParentAndChildA(parent, child);
        assertTrue(configurationTemplateManager.isOverridden(getPath(paths.first, "b")));
        assertFalse(configurationTemplateManager.isOverridden(getPath(paths.second, "b")));
    }

    public void testIsOverriddenOverriddenInGrandchild() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setB("pb");
        ConfigA child = new ConfigA("child");
        ConfigA grandchild = new ConfigA("grandchild");
        grandchild.setB("cb");
        String[] paths = insertParentChildAndGrandchildA(parent, child, grandchild);
        assertTrue(configurationTemplateManager.isOverridden(getPath(paths[0], "b")));
        assertTrue(configurationTemplateManager.isOverridden(getPath(paths[1], "b")));
        assertFalse(configurationTemplateManager.isOverridden(getPath(paths[2], "b")));
    }

    public void testIsOverriddenCompositeOverriddenInGrandchild() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setComposite(new ConfigB("overrideme"));
        ConfigA child = new ConfigA("child");
        ConfigA grandchild = new ConfigA("grandchild");
        ConfigB b = new ConfigB("overrideme");
        b.setB("hehe");
        grandchild.setComposite(b);
        String[] paths = insertParentChildAndGrandchildA(parent, child, grandchild);
        assertTrue(configurationTemplateManager.isOverridden(getPath(paths[0], "composite")));
        assertTrue(configurationTemplateManager.isOverridden(getPath(paths[1], "composite")));
        assertFalse(configurationTemplateManager.isOverridden(getPath(paths[2], "composite")));
    }

    public void testIsOverriddenCompositeHiddenInChild() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.getCs().put("hideme", new ConfigC("hideme"));
        ConfigA child = new ConfigA("child");
        ConfigA grandchild = new ConfigA("grandchild");
        String[] paths = insertParentChildAndGrandchildA(parent, child, grandchild);

        configurationTemplateManager.delete(getPath(paths[1], "cs", "hideme"));

        assertFalse(configurationTemplateManager.isOverridden(getPath(paths[0], "cs", "hideme")));
        assertFalse(configurationTemplateManager.isOverridden(getPath(paths[1], "cs", "hideme")));
        assertFalse(configurationTemplateManager.isOverridden(getPath(paths[2], "cs", "hideme")));
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
        ConfigB b = new ConfigB();
        b.setB("set");
        assertTrue(configurationTemplateManager.isDeeplyCompleteAndValid(b));
    }

    public void testIsDeeplyCompleteDirectInstanceIncomplete()
    {
        assertFalse(configurationTemplateManager.isDeeplyCompleteAndValid(new ConfigB()));
    }

    public void testIsDeeplyCompleteDirectInstanceInvalid()
    {
        ConfigB b = new ConfigB();
        b.setB("set");
        b.addFieldError("b", "nasty");
        assertFalse(configurationTemplateManager.isDeeplyCompleteAndValid(new ConfigB()));
    }

    public void testIsDeeplyCompleteIndirectInstanceComplete()
    {
        ConfigB b = new ConfigB();
        b.setB("set");
        ConfigA a = new ConfigA("a");
        a.setComposite(b);

        assertTrue(configurationTemplateManager.isDeeplyCompleteAndValid(a));
    }

    public void testIsDeeplyCompleteIndirectInstanceIncomplete()
    {
        ConfigA a = new ConfigA("a");
        a.setComposite(new ConfigB());

        assertFalse(configurationTemplateManager.isDeeplyCompleteAndValid(a));
    }

    public void testIsDeeplyCompleteIndirectInstanceInvalid()
    {
        ConfigB b = new ConfigB();
        b.setB("set");
        b.addFieldError("b", "nasty");
        ConfigA a = new ConfigA("a");
        a.setComposite(b);

        assertFalse(configurationTemplateManager.isDeeplyCompleteAndValid(a));
    }

    public void testIsDeeplyCompleteIndirectInstanceNull()
    {
        assertTrue(configurationTemplateManager.isDeeplyCompleteAndValid(new ConfigA("a")));
    }

    public void testGetRootInstanceNonTemplated()
    {
        try
        {
            configurationTemplateManager.getRootInstance(SCOPE_SAMPLE, ConfigA.class);
            fail("Can't get root of non-templated scope");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Path 'sample' does not refer to a templated collection", e.getMessage());
        }
    }

    public void testGetRootInstanceTemplatedEmpty() throws TypeException
    {
        assertNull(configurationTemplateManager.getRootInstance(SCOPE_TEMPLATED, ConfigA.class));
    }

    public void testGetRootInstanceTemplated() throws TypeException
    {
        insertParentAndChildA(new ConfigA("parent"), new ConfigA("child"));
        assertEquals("parent", configurationTemplateManager.getRootInstance(SCOPE_TEMPLATED, ConfigA.class).getName());
    }

    public void testCacheInsertUpdatesParent()
    {
        // Inserting a new collection entry should refresh the parent instance.
        Configuration collection = configurationTemplateManager.getInstance(SCOPE_SAMPLE);
        configurationTemplateManager.insert(SCOPE_SAMPLE, new ConfigA("bar"));
        assertNotSame(collection, configurationTemplateManager.getInstance(SCOPE_SAMPLE));
    }

    public void testCacheUpdateUpdatesParent()
    {
        // Updating a collection entry should refresh the parent instance.
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, new ConfigA("foo"));
        Configuration collection = configurationTemplateManager.getInstance(SCOPE_SAMPLE);

        ConfigA item = configurationTemplateManager.getInstance(path, ConfigA.class);
        item = configurationTemplateManager.deepClone(item);
        item.setB("hooray");
        configurationTemplateManager.save(item);

        assertNotSame(collection, configurationTemplateManager.getInstance(SCOPE_SAMPLE));
    }

    public void testCacheDeleteUpdatesParent()
    {
        // Deleting a collection entry should refresh the parent instance.
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, new ConfigA("foo"));
        Configuration collection = configurationTemplateManager.getInstance(SCOPE_SAMPLE);

        configurationTemplateManager.delete(path);

        assertNotSame(collection, configurationTemplateManager.getInstance(SCOPE_SAMPLE));
    }

    public void testCacheInsertLeavesSibling()
    {
        // Inserting a new collection entry should not cause existing items in
        // that collection to refresh.
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, new ConfigA("foo"));
        ConfigA sibling = configurationTemplateManager.getInstance(path, ConfigA.class);

        configurationTemplateManager.insert(SCOPE_SAMPLE, new ConfigA("bar"));
        assertSame(sibling, configurationTemplateManager.getInstance(path, ConfigA.class));
    }

    public void testCacheUpdateLeavesSibling()
    {
        // Updating a collection entry should not cause other items in that
        // collection to refresh.
        String unchangedPath = configurationTemplateManager.insert(SCOPE_SAMPLE, new ConfigA("foo"));
        String changedPath = configurationTemplateManager.insert(SCOPE_SAMPLE, new ConfigA("bar"));
        ConfigA unchangedInstance = configurationTemplateManager.getInstance(unchangedPath, ConfigA.class);

        ConfigA item = configurationTemplateManager.getInstance(changedPath, ConfigA.class);
        item = configurationTemplateManager.deepClone(item);
        item.setB("hooray");
        configurationTemplateManager.save(item);

        assertSame(unchangedInstance, configurationTemplateManager.getInstance(unchangedPath, ConfigA.class));
    }

    public void testCacheDeleteLeavesSibling()
    {
        // Deleting a collection entry should not cause other items in that
        // collection to refresh.
        String unchangedPath = configurationTemplateManager.insert(SCOPE_SAMPLE, new ConfigA("foo"));
        String deletedPath = configurationTemplateManager.insert(SCOPE_SAMPLE, new ConfigA("bar"));
        ConfigA unchangedInstance = configurationTemplateManager.getInstance(unchangedPath, ConfigA.class);

        configurationTemplateManager.delete(deletedPath);

        assertSame(unchangedInstance, configurationTemplateManager.getInstance(unchangedPath, ConfigA.class));
    }

    public void testCacheUpdateUpdatesReferers()
    {
        Referee ee = new Referee("ee");
        String refereePath = configurationTemplateManager.insert(SCOPE_REFEREE, ee);
        ee = configurationTemplateManager.getInstance(refereePath, Referee.class);

        Referer er = new Referer("er");
        er.setRefToRef(ee);

        String refererPath = configurationTemplateManager.insert(SCOPE_REFERER, er);
        updateRefereeAndCheck(refereePath, refererPath);
    }

    public void testCacheUpdateUpdatesReferersCollection()
    {
        Referee ee = new Referee("ee");
        String refereePath = configurationTemplateManager.insert(SCOPE_REFEREE, ee);
        ee = configurationTemplateManager.getInstance(refereePath, Referee.class);

        Referer er = new Referer("er");
        er.getRefToRefs().add(ee);

        String refererPath = configurationTemplateManager.insert(SCOPE_REFERER, er);
        updateRefereeAndCheck(refereePath, refererPath);
    }

    private void updateRefereeAndCheck(String refereePath, String refererPath)
    {
        Configuration erCollection = configurationTemplateManager.getInstance(SCOPE_REFERER);
        Referer er = configurationTemplateManager.getInstance(refererPath, Referer.class);

        Referee ee = configurationTemplateManager.getInstance(refereePath, Referee.class);
        ee = configurationTemplateManager.deepClone(ee);
        ee.setName("new");
        configurationTemplateManager.save(ee);

        assertNotSame(er, configurationTemplateManager.getInstance(refererPath, Referer.class));
        assertNotSame(erCollection, configurationTemplateManager.getInstance(SCOPE_REFERER));
    }

    public void testCacheDeleteUpdatesReferers()
    {
        Referee ee = new Referee("ee");
        String refereePath = configurationTemplateManager.insert(SCOPE_REFEREE, ee);
        ee = configurationTemplateManager.getInstance(refereePath, Referee.class);

        Referer er = new Referer("er");
        er.setRefToRef(ee);

        String refererPath = configurationTemplateManager.insert(SCOPE_REFERER, er);
        Configuration erCollection = configurationTemplateManager.getInstance(SCOPE_REFERER);
        er = configurationTemplateManager.getInstance(refererPath, Referer.class);

        configurationTemplateManager.delete(refereePath);

        assertNotSame(er, configurationTemplateManager.getInstance(refererPath, Referer.class));
        assertNotSame(erCollection, configurationTemplateManager.getInstance(SCOPE_REFERER));
    }

    public void testCacheUpdateUpdatesDescendants() throws TypeException
    {
        Pair<String, String> paths = insertParentAndChildA(new ConfigA("parent"), new ConfigA("child"));
        ConfigA child = configurationTemplateManager.getInstance(paths.second, ConfigA.class);

        ConfigA parent = configurationTemplateManager.getInstance(paths.first, ConfigA.class);
        parent = configurationTemplateManager.deepClone(parent);
        parent.setB("i haz change it");
        configurationTemplateManager.save(parent);

        assertNotSame(child, configurationTemplateManager.getInstance(paths.second, ConfigA.class));
    }

    public void testCacheUpdateLeavesAncestors() throws TypeException
    {
        Pair<String, String> paths = insertParentAndChildA(new ConfigA("parent"), new ConfigA("child"));
        ConfigA parent = configurationTemplateManager.getInstance(paths.first, ConfigA.class);

        ConfigA child = configurationTemplateManager.getInstance(paths.second, ConfigA.class);
        child = configurationTemplateManager.deepClone(child);
        child.setB("i haz change it");
        configurationTemplateManager.save(child);

        assertSame(parent, configurationTemplateManager.getInstance(paths.first, ConfigA.class));
    }

    public void testCacheNestedUpdateWithDescendants() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.getCs().put("c1", new ConfigC("c1"));
        parent.getCs().put("c2", new ConfigC("c2"));
        Pair<String, String> paths = insertParentAndChildA(parent, new ConfigA("child"));
        ConfigA child = configurationTemplateManager.getInstance(paths.second, ConfigA.class);

        parent = configurationTemplateManager.getInstance(paths.first, ConfigA.class);
        parent = configurationTemplateManager.deepClone(parent);
        parent.getCs().get("c1").setD(new ConfigD("new"));
        configurationTemplateManager.save(parent);

        ConfigA newChild = configurationTemplateManager.getInstance(paths.second, ConfigA.class);
        assertNotSame(child, newChild);
        assertNotSame(child.getCs(), newChild.getCs());
        assertNotSame(child.getCs().get("c1"), newChild.getCs().get("c1"));
        assertSame(child.getCs().get("c2"), newChild.getCs().get("c2"));
    }

    public void testGetHighestNoneSatisfy() throws TypeException
    {
        insertParentChildAndGrandchildA(new ConfigA(GRANDPARENT_NAME), new ConfigA(CHILD_NAME), new ConfigA(GRANDCHILD_NAME));
        assertEquals(0, configurationTemplateManager.getHighestInstancesSatisfying(new FalsePredicate<ConfigA>(), ConfigA.class).size());
    }

    public void testGetHighestAllSatisfy() throws TypeException
    {
        insertParentChildAndGrandchildA(new ConfigA(GRANDPARENT_NAME), new ConfigA(CHILD_NAME), new ConfigA(GRANDCHILD_NAME));
        List<ConfigA> results = configurationTemplateManager.getHighestInstancesSatisfying(new TruePredicate<ConfigA>(), ConfigA.class);
        assertEquals(1, results.size());
        assertEquals(GRANDPARENT_NAME, results.get(0).getName());
    }

    public void testGetHighestGranchildSatisfies() throws TypeException
    {
        insertParentChildAndGrandchildA(new ConfigA(GRANDPARENT_NAME), new ConfigA(CHILD_NAME), new ConfigA(GRANDCHILD_NAME));
        List<ConfigA> results = configurationTemplateManager.getHighestInstancesSatisfying(new Predicate<ConfigA>()
        {
            public boolean satisfied(ConfigA mockA)
            {
                return mockA.getName().equals(GRANDCHILD_NAME);
            }
        }, ConfigA.class);

        assertEquals(1, results.size());
        assertEquals(GRANDCHILD_NAME, results.get(0).getName());
    }

    public void testGetHighestChildSatisfies() throws TypeException
    {
        insertParentChildAndGrandchildA(new ConfigA(GRANDPARENT_NAME), new ConfigA(CHILD_NAME), new ConfigA(GRANDCHILD_NAME));
        List<ConfigA> results = configurationTemplateManager.getHighestInstancesSatisfying(new Predicate<ConfigA>()
        {
            public boolean satisfied(ConfigA mockA)
            {
                return mockA.getName().equals(CHILD_NAME);
            }
        }, ConfigA.class);

        assertEquals(1, results.size());
        assertEquals(CHILD_NAME, results.get(0).getName());
    }

    public void testGetHighestChildAndGrandchildSatisfies() throws TypeException
    {
        insertParentChildAndGrandchildA(new ConfigA(GRANDPARENT_NAME), new ConfigA(CHILD_NAME), new ConfigA(GRANDCHILD_NAME));
        List<ConfigA> results = configurationTemplateManager.getHighestInstancesSatisfying(new Predicate<ConfigA>()
        {
            public boolean satisfied(ConfigA mockA)
            {
                return !mockA.getName().equals(GRANDPARENT_NAME);
            }
        }, ConfigA.class);

        assertEquals(1, results.size());
        assertEquals(CHILD_NAME, results.get(0).getName());
    }

    public void testGetAncestorPathsInvalidPath()
    {
        try
        {
            configurationTemplateManager.getAncestorPaths("invalid/path", true);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage(), containsString("Invalid path 'invalid/path': references unknown scope 'invalid'"));
        }
    }
    
    public void testGetAncestorPathsRoot() throws TypeException
    {
        Pair<String, String> paths = insertParentAndChildA(new ConfigA("parent"), new ConfigA("child"));
        assertEquals(Arrays.<String>asList(), configurationTemplateManager.getAncestorPaths(paths.first, true));
    }

    public void testGetAncestorPathsRootNonStrict() throws TypeException
    {
        Pair<String, String> paths = insertParentAndChildA(new ConfigA("parent"), new ConfigA("child"));
        assertEquals(asList(paths.first), configurationTemplateManager.getAncestorPaths(paths.first, false));
    }

    public void testGetAncestorPathsChild() throws TypeException
    {
        Pair<String, String> paths = insertParentAndChildA(new ConfigA("parent"), new ConfigA("child"));
        assertEquals(asList(paths.first), configurationTemplateManager.getAncestorPaths(paths.second, true));
    }

    public void testGetAncestorPathsChildNonStrict() throws TypeException
    {
        Pair<String, String> paths = insertParentAndChildA(new ConfigA("parent"), new ConfigA("child"));
        assertEquals(asList(paths.second, paths.first), configurationTemplateManager.getAncestorPaths(paths.second, false));
    }

    public void testGetAncestorPathsNestedInRoot() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setComposite(new ConfigB("x"));
        Pair<String, String> paths = insertParentAndChildA(parent, new ConfigA("child"));
        assertEquals(Arrays.<String>asList(), configurationTemplateManager.getAncestorPaths(getPath(paths.first, "composite"), true));
    }

    public void testGetAncestorPathsNestedInRootNonStrict() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setComposite(new ConfigB("x"));
        Pair<String, String> paths = insertParentAndChildA(parent, new ConfigA("child"));
        String parentBPath = getPath(paths.first, "composite");
        assertEquals(asList(parentBPath), configurationTemplateManager.getAncestorPaths(parentBPath, false));
    }

    public void testGetAncestorPathsNestedInChild() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setComposite(new ConfigB("x"));
        Pair<String, String> paths = insertParentAndChildA(parent, new ConfigA("child"));
        assertEquals(asList(getPath(paths.first, "composite")), configurationTemplateManager.getAncestorPaths(getPath(paths.second, "composite"), true));
    }

    public void testGetAncestorPathsNestedInChildNonStrict() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setComposite(new ConfigB("x"));
        Pair<String, String> paths = insertParentAndChildA(parent, new ConfigA("child"));
        String childBPath = getPath(paths.second, "composite");
        assertEquals(asList(childBPath, getPath(paths.first, "composite")), configurationTemplateManager.getAncestorPaths(childBPath, false));
    }

    public void testGetAncestorPathsMultipleAncestors() throws TypeException
    {
        String[] paths = insertParentChildAndGrandchildA(new ConfigA("parent"), new ConfigA("child"), new ConfigA("grandchild"));
        assertEquals(asList(paths[1], paths[0]), configurationTemplateManager.getAncestorPaths(paths[2], true));
    }

    public void testGetAncestorPathsNestedMultipleAncestors() throws TypeException
    {
        ConfigA parent = new ConfigA("parent");
        parent.setComposite(new ConfigB("x"));
        String[] paths = insertParentChildAndGrandchildA(parent, new ConfigA("child"), new ConfigA("grandchild"));
        assertEquals(asList(getPath(paths[1], "composite"), getPath(paths[0], "composite")), configurationTemplateManager.getAncestorPaths(getPath(paths[2], "composite"), true));
    }
    
    private Pair<String, String> insertParentAndChildA(ConfigA parent, ConfigA child) throws TypeException
    {
        MutableRecord record = unstantiate(parent);
        configurationTemplateManager.markAsTemplate(record);
        String parentPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);
        long parentHandle = recordManager.select(parentPath).getHandle();

        record = unstantiate(child);
        configurationTemplateManager.setParentTemplate(record, parentHandle);
        return new Pair<String, String>(parentPath, configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record));
    }

    private String[] insertParentChildAndGrandchildA(ConfigA parent, ConfigA child, ConfigA grandchild) throws TypeException
    {
        MutableRecord record = unstantiate(parent);
        configurationTemplateManager.markAsTemplate(record);
        String parentPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);
        long parentHandle = recordManager.select(parentPath).getHandle();

        record = unstantiate(child);
        configurationTemplateManager.markAsTemplate(record);
        configurationTemplateManager.setParentTemplate(record, parentHandle);
        String childPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, record);
        long childHandle = recordManager.select(childPath).getHandle();

        record = unstantiate(grandchild);
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

    @SymbolicName("configA")
    public static class ConfigA extends AbstractNamedConfiguration
    {
        private String b;
        private String c;

        private ConfigB composite;
        private Map<String, ConfigC> cs = new HashMap<String, ConfigC>();
        private List<ConfigD> ds = new LinkedList<ConfigD>();
        private List<String> pl = new LinkedList<String>();

        public ConfigA()
        {
        }

        public ConfigA(String name)
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

        public ConfigB getComposite()
        {
            return composite;
        }

        public void setComposite(ConfigB composite)
        {
            this.composite = composite;
        }

        public Map<String, ConfigC> getCs()
        {
            return cs;
        }

        public void setCs(Map<String, ConfigC> cs)
        {
            this.cs = cs;
        }

        public List<ConfigD> getDs()
        {
            return ds;
        }

        public void setDs(List<ConfigD> ds)
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

    @SymbolicName("configB")
    public static class ConfigB extends AbstractConfiguration
    {
        @Required
        private String b;

        public ConfigB()
        {
        }

        public ConfigB(String b)
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

    @SymbolicName("configC")
    public static class ConfigC extends AbstractNamedConfiguration
    {
        private ConfigD d;

        public ConfigC()
        {
        }

        public ConfigC(String name)
        {
            super(name);
        }

        public ConfigD getD()
        {
            return d;
        }

        public void setD(ConfigD d)
        {
            this.d = d;
        }
    }

    @SymbolicName("configD")
    public static class ConfigD extends AbstractNamedConfiguration
    {
        public ConfigD()
        {
        }

        public ConfigD(String name)
        {
            super(name);
        }
    }

    @SymbolicName("referer")
    public static class Referer extends AbstractNamedConfiguration
    {
        Referee ref;
        @Reference
        Referee refToRef;
        @Reference
        List<Referee> refToRefs = new LinkedList<Referee>();

        public Referer()
        {
        }

        public Referer(String name)
        {
            super(name);
        }

        public Referee getRef()
        {
            return ref;
        }

        public void setRef(Referee ref)
        {
            this.ref = ref;
        }

        public Referee getRefToRef()
        {
            return refToRef;
        }

        public void setRefToRef(Referee refToRef)
        {
            this.refToRef = refToRef;
        }

        public List<Referee> getRefToRefs()
        {
            return refToRefs;
        }

        public void setRefToRefs(List<Referee> refToRefs)
        {
            this.refToRefs = refToRefs;
        }
    }

    @SymbolicName("referee")
    public static class Referee extends AbstractNamedConfiguration
    {
        public Referee()
        {
        }

        public Referee(String name)
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
