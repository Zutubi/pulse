package com.zutubi.prototype.config;

import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.Internal;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.TemplatedMapType;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.config.Configuration;
import junit.framework.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class TypeListenerTest extends AbstractConfigurationSystemTestCase
{
    private DefaultConfigurationProvider provider = null;
    private CompositeType typeA;
    private CompositeType typeB;
    private long globalHandle;
    private long childtHandle;

    protected void setUp() throws Exception
    {
        super.setUp();

        provider = new DefaultConfigurationProvider();
        provider.setEventManager(eventManager);
        provider.setTypeRegistry(typeRegistry);
        provider.setConfigurationPersistenceManager(configurationPersistenceManager);
        provider.setConfigurationTemplateManager(configurationTemplateManager);
        provider.init();

        typeA = typeRegistry.register(A.class);
        typeB = typeRegistry.getType(B.class);

        MapType mapA = new MapType();
        mapA.setTypeRegistry(typeRegistry);
        mapA.setCollectionType(typeA);
        configurationPersistenceManager.register("sample", mapA);

        MapType templatedMap = new TemplatedMapType();
        templatedMap.setTypeRegistry(typeRegistry);
        templatedMap.setCollectionType(typeA);
        configurationPersistenceManager.register("template", templatedMap);

        insertA("globalt", -1, true);

        globalHandle = configurationTemplateManager.getRecord("template/globalt").getHandle();
        insertA("childt", globalHandle, true);
        insertA("child", globalHandle, false);

        childtHandle = configurationTemplateManager.getRecord("template/childt").getHandle();
        insertA("grandchildt", childtHandle, true);
        insertA("grandchild", childtHandle, false);
    }

    private void insertA(String name, long parentHandle, boolean template)
    {
        MutableRecord record = createA(name);
        if (template)
        {
            configurationTemplateManager.markAsTemplate(record);
        }
        if (parentHandle >= 0)
        {
            configurationTemplateManager.setParentTemplate(record, parentHandle);
        }
        configurationTemplateManager.insertRecord("template", record);
    }

    private MutableRecord createA(String name)
    {
        MutableRecord record = typeA.createNewRecord(false);
        record.put("name", name);
        return record;
    }

    private void insertB(String name, String path)
    {
        MutableRecord record = createB(name);
        configurationTemplateManager.insertRecord(path, record);
    }

    private MutableRecord createB(String name)
    {
        MutableRecord record = typeB.createNewRecord(false);
        record.put("name", name);
        return record;
    }

    protected void tearDown() throws Exception
    {
        provider = null;
        super.tearDown();
    }

    public void testInstanceInserted()
    {
        MockTypeListener<A> listener = register(A.class);
        configurationTemplateManager.insert("sample", new A("a"));
        listener.assertInsert("sample/a");
        listener.assertDone();
    }

    public void testInstanceChanged()
    {
        String path = configurationTemplateManager.insert("sample", new A("a"));
        MockTypeListener<A> listener = register(A.class);

        configurationTemplateManager.save(configurationTemplateManager.getCloneOfInstance(path, A.class));
        listener.assertSave("sample/a");
        listener.assertDone();
    }

    public void testInstanceDeleted()
    {
        configurationTemplateManager.insert("sample", new A("a"));
        MockTypeListener<A> listener = register(A.class);
        configurationTemplateManager.delete("sample/a");
        listener.assertDelete("sample/a");
        listener.assertDone();
    }

    public void testNestedInsertTriggersSave()
    {
        configurationTemplateManager.insert("sample", new A("a"));
        MockTypeListener<A> listener = register(A.class);
        configurationTemplateManager.insert("sample/a/b", new B("b"));
        listener.assertSave("sample/a");
        listener.assertDone();
    }

    public void testNestedSaveTriggersSave()
    {
        configurationTemplateManager.insert("sample", new A("a"));
        String bPath = configurationTemplateManager.insert("sample/a/b", new B("b"));
        MockTypeListener<A> listener = register(A.class);
        configurationTemplateManager.save(configurationTemplateManager.getCloneOfInstance(bPath, B.class));
        listener.assertSave("sample/a");
        listener.assertDone();
    }

    public void testNestedDeleteTriggersSave()
    {
        configurationTemplateManager.insert("sample", new A("a"));
        configurationTemplateManager.insert("sample/a/b", new B("b"));
        MockTypeListener<A> listener = register(A.class);
        configurationTemplateManager.delete("sample/a/b");
        listener.assertSave("sample/a");
        listener.assertDone();
    }

    public void testInsertTemplateA()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        insertA("test", globalHandle, true);
        la.assertDone();
        lb.assertDone();
    }

    public void testInsertTemplateAInheritingB()
    {
        insertB("nesty", "template/globalt/b");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        insertA("test", globalHandle, true);
        la.assertDone();
        lb.assertDone();
    }

    public void testInsertConcreteA()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        insertA("test", globalHandle, false);
        la.assertInsert("template/test");
        la.assertDone();
        lb.assertDone();
    }

    public void testInsertConcreteAInheritingB()
    {
        insertB("nesty", "template/globalt/b");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        insertA("test", globalHandle, false);
        la.assertInsert("template/test");
        la.assertDone();
        lb.assertInsert("template/test/b");
        lb.assertDone();
    }

    public void testInsertConcreteAInheritingBFromCollection()
    {
        insertB("nesty", "template/globalt/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        insertA("test", globalHandle, false);
        la.assertInsert("template/test");
        la.assertDone();
        lb.assertInsert("template/test/bees/nesty");
        lb.assertDone();
    }

    public void testInsertBIntoLeaf()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        insertB("test", "template/child/b");
        la.assertSave("template/child");
        la.assertDone();
        lb.assertInsert("template/child/b");
        lb.assertDone();
    }

    public void testInsertBIntoLeafCollection()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        insertB("test", "template/child/bees");
        la.assertSave("template/child");
        la.assertDone();
        lb.assertInsert("template/child/bees/test");
        lb.assertDone();
    }

    public void testInsertBIntoIntermediate()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        insertB("test", "template/childt/b");
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertInsert("template/grandchild/b");
        lb.assertDone();
    }

    public void testInsertBIntoIntermediateCollection()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        insertB("test", "template/childt/bees");
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertInsert("template/grandchild/bees/test");
        lb.assertDone();
    }
    
    public void testInsertBIntoRoot()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        insertB("test", "template/globalt/b");
        la.assertSave("template/grandchild");
        la.assertSave("template/child");
        la.assertDone();
        lb.assertInsert("template/grandchild/b");
        lb.assertInsert("template/child/b");
        lb.assertDone();
    }

    public void testInsertBIntoRootCollection()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        insertB("test", "template/globalt/bees");
        la.assertSave("template/grandchild");
        la.assertSave("template/child");
        la.assertDone();
        lb.assertInsert("template/grandchild/bees/test");
        lb.assertInsert("template/child/bees/test");
        lb.assertDone();
    }

    public void testSaveConcreteA()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/child", createA("child"));
        la.assertSave("template/child");
        la.assertDone();
        lb.assertDone();
    }

    public void testRenameConcreteA()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/child", createA("newname"));
        la.assertSave("template/newname");
        la.assertDone();
        lb.assertDone();
    }

    public void testSaveLeafTemplateA()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/grandchildt", createA("grandchildt"));
        la.assertDone();
        lb.assertDone();
    }

    public void testRenameLeafTemplateA()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/grandchildt", createA("newname"));
        la.assertDone();
        lb.assertDone();
    }

    public void testSaveIntermediateTemplateA()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt", createA("childt"));
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertDone();
    }

    public void testRenameIntermediateTemplateA()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt", createA("newname"));
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertDone();
    }

    public void testSaveRootTemplateA()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/globalt", createA("globalt"));
        la.assertSave("template/grandchild");
        la.assertSave("template/child");
        la.assertDone();
        lb.assertDone();
    }

    public void testRenameRootTemplateA()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/globalt", createA("newname"));
        la.assertSave("template/grandchild");
        la.assertSave("template/child");
        la.assertDone();
        lb.assertDone();
    }

    public void testSaveBInLeaf()
    {
        insertB("test", "template/child/b");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/child/b", createB("test"));
        la.assertSave("template/child");
        la.assertDone();
        lb.assertSave("template/child/b");
        lb.assertDone();
    }

    public void testSaveBInLeafCollection()
    {
        insertB("test", "template/child/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/child/bees/test", createB("test"));
        la.assertSave("template/child");
        la.assertDone();
        lb.assertSave("template/child/bees/test");
        lb.assertDone();
    }

    public void testRenameBInLeafCollection()
    {
        insertB("test", "template/child/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/child/bees/test", createB("newname"));
        la.assertSave("template/child");
        la.assertDone();
        lb.assertSave("template/child/bees/newname");
        lb.assertDone();
    }

    public void testSaveBInIntermediate()
    {
        insertB("test", "template/childt/b");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt/b", createB("test"));
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertSave("template/grandchild/b");
        lb.assertDone();
    }

    public void testSaveBInIntermediateCollection()
    {
        insertB("test", "template/childt/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt/bees/test", createB("test"));
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertSave("template/grandchild/bees/test");
        lb.assertDone();
    }

    public void testRenameBInIntermediateCollection()
    {
        insertB("test", "template/childt/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt/bees/test", createB("newname"));
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertSave("template/grandchild/bees/newname");
        lb.assertDone();
    }

    public void testSaveBInRoot()
    {
        insertB("test", "template/globalt/b");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/globalt/b", createB("test"));
        la.assertSave("template/grandchild");
        la.assertSave("template/child");
        la.assertDone();
        lb.assertSave("template/grandchild/b");
        lb.assertSave("template/child/b");
        lb.assertDone();
    }

    public void testSaveBInRootCollection()
    {
        insertB("test", "template/globalt/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/globalt/bees/test", createB("test"));
        la.assertSave("template/grandchild");
        la.assertSave("template/child");
        la.assertDone();
        lb.assertSave("template/grandchild/bees/test");
        lb.assertSave("template/child/bees/test");
        lb.assertDone();
    }

    public void testRenameBInRootCollection()
    {
        insertB("test", "template/globalt/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/globalt/bees/test", createB("newname"));
        la.assertSave("template/grandchild");
        la.assertSave("template/child");
        la.assertDone();
        lb.assertSave("template/grandchild/bees/newname");
        lb.assertSave("template/child/bees/newname");
        lb.assertDone();
    }

    public void testDeleteConcreteA()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/child");
        la.assertDelete("template/child");
        la.assertDone();
        lb.assertDone();
    }

    public void testDeleteConcreteAIncludingB()
    {
        insertB("test", "template/child/b");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/child");
        la.assertDelete("template/child");
        la.assertDone();
        lb.assertDelete("template/child/b");
        lb.assertDone();
    }

    public void testDeleteConcreteAIncludingBInCollection()
    {
        insertB("test", "template/child/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/child");
        la.assertDelete("template/child");
        la.assertDone();
        lb.assertDelete("template/child/bees/test");
        lb.assertDone();
    }

    public void testDeleteTemplateA()
    {
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/grandchildt");
        la.assertDone();
        lb.assertDone();
    }

    public void testDeleteTemplateAIncludingB()
    {
        insertB("test", "template/grandchildt/b");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/grandchildt");
        la.assertDone();
        lb.assertDone();
    }

    public void testDeleteTemplateAIncludingBInCollection()
    {
        insertB("test", "template/grandchildt/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/grandchildt");
        la.assertDone();
        lb.assertDone();
    }

    public void testDeleteBFromLeaf()
    {
        insertB("test", "template/grandchild/b");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/grandchild/b");
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertDelete("template/grandchild/b");
        lb.assertDone();
    }

    public void testDeleteBFromLeafCollection()
    {
        insertB("test", "template/grandchild/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/grandchild/bees/test");
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertDelete("template/grandchild/bees/test");
        lb.assertDone();
    }

    public void testDeleteBFromIntermediate()
    {
        insertB("test", "template/childt/b");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/childt/b");
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertDelete("template/grandchild/b");
        lb.assertDone();
    }

    public void testDeleteBFromIntermediateCollection()
    {
        insertB("test", "template/childt/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/childt/bees/test");
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertDelete("template/grandchild/bees/test");
        lb.assertDone();
    }

    public void testDeleteBFromRoot()
    {
        insertB("test", "template/globalt/b");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/globalt/b");
        la.assertSave("template/grandchild");
        la.assertSave("template/child");
        la.assertDone();
        lb.assertDelete("template/grandchild/b");
        lb.assertDelete("template/child/b");
        lb.assertDone();
    }

    public void testDeleteBFromRootCollection()
    {
        insertB("test", "template/globalt/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/globalt/bees/test");
        la.assertSave("template/grandchild");
        la.assertSave("template/child");
        la.assertDone();
        lb.assertDelete("template/grandchild/bees/test");
        lb.assertDelete("template/child/bees/test");
        lb.assertDone();
    }

    public void testSaveBInLeafOverridingIntermediate()
    {
        insertB("test", "template/childt/b");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/grandchild/b", createB("test"));
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertSave("template/grandchild/b");
        lb.assertDone();
    }

    public void testSaveBInLeafCollectionOverridingIntermediate()
    {
        insertB("test", "template/childt/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/grandchild/bees/test", createB("test"));
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertSave("template/grandchild/bees/test");
        lb.assertDone();
    }

    public void testSaveBInIntermediateOverridingRoot()
    {
        insertB("test", "template/globalt/b");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt/b", createB("test"));
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertSave("template/grandchild/b");
        lb.assertDone();
    }

    public void testSaveBInIntermediateCollectionOverridingRoot()
    {
        insertB("test", "template/globalt/bees");
        MockTypeListener<A> la = register(A.class);
        MockTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt/bees/test", createB("test"));
        la.assertSave("template/grandchild");
        la.assertDone();
        lb.assertSave("template/grandchild/bees/test");
        lb.assertDone();
    }

    public void testAssignId()
    {
        IDAssigningListener<A> listener = new IDAssigningListener<A>(A.class);
        listener.register(provider);

        configurationTemplateManager.insertRecord("sample", createA("a"));
        assertId("sample/a", 1);
    }

    public void testAssignedIdSurvivesRefresh()
    {
        IDAssigningListener<A> listener = new IDAssigningListener<A>(A.class);
        listener.register(provider);

        configurationTemplateManager.insertRecord("sample", createA("a"));
        configurationTemplateManager.saveRecord("sample/a", createA("a"));
        assertId("sample/a", 1);
    }

    public void testNewConcreteAssignedId()
    {
        IDAssigningListener<A> listener = new IDAssigningListener<A>(A.class);
        listener.register(provider);

        insertA("new", childtHandle, false);
        assertId("template/new", 1);
    }

    public void testAllConcreteGetAssignedIds()
    {
        IDAssigningListener<B> listener = new IDAssigningListener<B>(B.class);
        listener.register(provider);

        insertB("new", "template/globalt/b");
        assertId("template/grandchild/b", 1);
        assertId("template/child/b", 2);
    }
    
    private void assertId(String path, int id)
    {
        EyeDee c = (EyeDee) configurationTemplateManager.getInstance(path);
        assertNotNull(c);
        assertEquals(id, c.getId());
    }

    private <T extends Configuration> MockTypeListener<T> register(Class<T> clazz)
    {
        MockTypeListener<T> listener = new MockTypeListener<T>(clazz);
        listener.register(provider);
        return listener;
    }

    private static class MockTypeListener<X extends Configuration> extends TypeListener<X>
    {
        private static class Event
        {
            public enum Type
            {
                INSERT,
                DELETE,
                SAVE
            }

            public Type type;
            public String path;

            public Event(Type type, String path)
            {
                this.type = type;
                this.path = path;
            }

            public boolean equals(Object o)
            {
                if (this == o)
                {
                    return true;
                }
                if (o == null || getClass() != o.getClass())
                {
                    return false;
                }

                Event event = (Event) o;

                return !(path != null ? !path.equals(event.path) : event.path != null) && type == event.type;
            }

            public int hashCode()
            {
                int result;
                result = (type != null ? type.hashCode() : 0);
                result = 31 * result + (path != null ? path.hashCode() : 0);
                return result;
            }
        }

        private List<Event> got = new LinkedList<Event>();

        public MockTypeListener(Class<X> configurationClass)
        {
            super(configurationClass);
        }

        public void postInsert(X instance)
        {
            got.add(new Event(Event.Type.INSERT, instance.getConfigurationPath()));
        }

        public void preDelete(X instance)
        {
            got.add(new Event(Event.Type.DELETE, instance.getConfigurationPath()));
        }

        public void postSave(X instance)
        {
            got.add(new Event(Event.Type.SAVE, instance.getConfigurationPath()));
        }

        private void assertEvent(String path, Event.Type type)
        {
            assertTrue(got.remove(new Event(type, path)));
        }

        public void assertInsert(String path)
        {
            assertEvent(path, Event.Type.INSERT);
        }

        public void assertDelete(String path)
        {
            assertEvent(path, Event.Type.DELETE);
        }

        public void assertSave(String path)
        {
            assertEvent(path, Event.Type.SAVE);
        }

        public void assertDone()
        {
            Assert.assertEquals(0, got.size());
        }
    }

    private static class IDAssigningListener<X extends EyeDee> extends MockTypeListener<X>
    {
        private long nextId = 1;

        public IDAssigningListener(Class<X> configurationClass)
        {
            super(configurationClass);
        }

        public void postInsert(EyeDee instance)
        {
            instance.setId(nextId++);
        }
    }

    public static abstract class EyeDee extends AbstractConfiguration
    {
        @Internal
        private long id;
        public long getId(){return id;}
        public void setId(long id){this.id = id;}
    }

    @SymbolicName("a")
    public static class A extends EyeDee
    {
        @ID
        private String name;
        private B b;
        private Map<String, B> bees;
        public A(){}
        public A(String a){this.name = a;}
        public String getName(){return name;}
        public void setName(String name){this.name = name;}
        public B getB(){return b;}
        public void setB(B b){this.b = b;}
        public Map<String, B> getBees(){return bees;}
        public void setBees(Map<String, B> bees){this.bees = bees;}
    }

    @SymbolicName("b")
    public static class B extends EyeDee
    {
        @ID
        private String name;
        public B(){}
        public B(String str){this.name = str;}
        public String getName(){return name;}
        public void setName(String name){this.name = name;}
    }
}

