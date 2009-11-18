package com.zutubi.tove.config;

import com.zutubi.tove.annotations.ID;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.events.*;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.TemplatedMapType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import static java.util.Arrays.asList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class DefaultConfigurationProviderTest extends AbstractConfigurationSystemTestCase
{
    private static final String SCOPE_TEMPLATE = "template";
    private static final String SCOPE_SAMPLE = "sample";

    private DefaultConfigurationProvider provider;
    private CompositeType typeA;

    protected void setUp() throws Exception
    {
        super.setUp();

        provider = new DefaultConfigurationProvider();
        provider.setEventManager(eventManager);
        provider.setTypeRegistry(typeRegistry);
        provider.setConfigurationPersistenceManager(configurationPersistenceManager);
        provider.setConfigurationTemplateManager(configurationTemplateManager);
        provider.setConfigurationStateManager(configurationStateManager);
        provider.setThreadFactory(Executors.defaultThreadFactory());
        provider.init();
        
        typeRegistry.register(A.class);
        
        C c = new C("c");
        B b = new B("b");
        A a = new A("a");
        a.setB(b);
        b.setC(c);

        typeA = typeRegistry.getType(A.class);
        MapType mapA = new MapType(typeA, typeRegistry);
        configurationPersistenceManager.register(SCOPE_SAMPLE, mapA);

        MapType templatedMap = new TemplatedMapType(typeA, typeRegistry);
        configurationPersistenceManager.register(SCOPE_TEMPLATE, templatedMap);
    }

    public void testRegisterListenerByClass()
    {
        // check that we receive the save events.
        MockConfigurationEventListener listener = new MockConfigurationEventListener();
        provider.registerEventListener(listener, true, A.class);

        A a = new A("a");

        // check the insert events.
        String path = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        listener.assertNextEvent(InsertEvent.class, "sample/a");
        listener.assertNextEvent(PostInsertEvent.class, "sample/a");
        listener.assertNoMoreEvents();

        // check the save events.
        a = configurationTemplateManager.getCloneOfInstance(path, A.class);
        a.setField("edited");
        configurationTemplateManager.save(a);
        listener.assertNextEvent(SaveEvent.class, "sample/edited");
        listener.assertNextEvent(PostSaveEvent.class, "sample/edited");
        listener.assertNoMoreEvents();

        // check the delete events.
        configurationTemplateManager.delete("sample/edited");
        listener.assertNextEvent(DeleteEvent.class, "sample/edited");
        listener.assertNextEvent(PostDeleteEvent.class, "sample/edited");
        listener.assertNoMoreEvents();
    }

    public void testRegisterListenerByPath()
    {
        // check that we receive the save events.
        MockConfigurationEventListener includingChildren = new MockConfigurationEventListener();
        provider.registerEventListener(includingChildren, true, true, SCOPE_SAMPLE);

        MockConfigurationEventListener excludingChildren = new MockConfigurationEventListener();
        provider.registerEventListener(excludingChildren, true, false, SCOPE_SAMPLE);

        A a = new A("a");

        // check the insert events.
        String aPath = configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        includingChildren.assertNextEvent(InsertEvent.class, "sample/a");
        includingChildren.assertNextEvent(PostInsertEvent.class, "sample/a");
        includingChildren.assertNoMoreEvents();
        excludingChildren.assertNoMoreEvents();

        configurationTemplateManager.insert("sample/a/b", new B("b"));
        includingChildren.assertNextEvent(InsertEvent.class, "sample/a/b");
        includingChildren.assertNextEvent(PostInsertEvent.class, "sample/a/b");
        includingChildren.assertNoMoreEvents();
        excludingChildren.assertNoMoreEvents();

        // check the save events.
        a = configurationTemplateManager.getCloneOfInstance(aPath, A.class);
        a.setField("edited");
        configurationTemplateManager.save(a);
        // including listening at "sample", will see everything that happens below it.
        includingChildren.assertNextEvent(SaveEvent.class, "sample/edited");
        includingChildren.assertNextEvent(PostSaveEvent.class, "sample/edited");
        includingChildren.assertNoMoreEvents();
        // excludingChildren listening at "sample", will not see changes to "sample/a"
        excludingChildren.assertNoMoreEvents();

        B b = configurationTemplateManager.getCloneOfInstance("sample/edited/b", B.class);
        b.setField("edited");
        configurationTemplateManager.save(b);
        includingChildren.assertNextEvent(SaveEvent.class, "sample/edited/b");
        includingChildren.assertNextEvent(PostSaveEvent.class, "sample/edited/b");
        includingChildren.assertNoMoreEvents();
        excludingChildren.assertNoMoreEvents();

        // check the delete events.
        configurationTemplateManager.delete("sample/edited");
        includingChildren.assertNextEvent(DeleteEvent.class, "sample/edited");
        includingChildren.assertNextEvent(DeleteEvent.class, "sample/edited/b");
        includingChildren.assertNextEvent(PostDeleteEvent.class, "sample/edited");
        includingChildren.assertNextEvent(PostDeleteEvent.class, "sample/edited/b");
        includingChildren.assertNoMoreEvents();
        // excludingChildren listenening at "sample", will not see changes to "sample/a"
        excludingChildren.assertNoMoreEvents();
    }

    public void testNotificationsForNestedClassWhenAncestorIsModified()
    {
        // check that we receive the save events.
        MockConfigurationEventListener listener = new MockConfigurationEventListener();
        provider.registerEventListener(listener, true, false, B.class);

        A a = new A("a");
        a.setB(new B("b"));

        // check the insert event.
        configurationTemplateManager.insert(SCOPE_SAMPLE, a);
        listener.assertNextEvent(InsertEvent.class, "sample/a/b");
        listener.assertNextEvent(PostInsertEvent.class, "sample/a/b");
        listener.assertNoMoreEvents();

        configurationTemplateManager.delete("sample/a");
        listener.assertNextEvent(DeleteEvent.class, "sample/a/b");
        listener.assertNextEvent(PostDeleteEvent.class, "sample/a/b");
        listener.assertNoMoreEvents();
    }

    public void testGetAllDescendents() throws TypeException
    {
        MutableRecord record = unstantiate(new A("global"));
        configurationTemplateManager.markAsTemplate(record);

        String globalPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATE, record);
        long globalHandle = configurationTemplateManager.getRecord(globalPath).getHandle();

        String childPath = insertConcreteChild(globalHandle, "concrete-child");

        record = unstantiate(new A("template-child"));
        configurationTemplateManager.markAsTemplate(record);
        configurationTemplateManager.setParentTemplate(record, globalHandle);
        String templatePath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATE, record);

        long templateHandle = configurationTemplateManager.getRecord(templatePath).getHandle();

        String grandchild1Path = insertConcreteChild(templateHandle, "concrete-grandchild1");
        String grandchild2Path = insertConcreteChild(templateHandle, "concrete-grandchild2");

        assertEquals(new HashSet<String>(asList(childPath, templatePath, grandchild1Path, grandchild2Path)), getAllDescendentPaths(globalPath, true, false));
        assertEquals(new HashSet<String>(asList(globalPath, childPath, templatePath, grandchild1Path, grandchild2Path)), getAllDescendentPaths(globalPath, false, false));
        assertEquals(new HashSet<String>(asList(childPath, grandchild1Path, grandchild2Path)), getAllDescendentPaths(globalPath, true, true));
        assertEquals(new HashSet<String>(asList(childPath, grandchild1Path, grandchild2Path)), getAllDescendentPaths(globalPath, false, true));

        assertEquals(new HashSet<String>(asList(grandchild1Path, grandchild2Path)), getAllDescendentPaths(templatePath, true, false));
        assertEquals(new HashSet<String>(asList(templatePath, grandchild1Path, grandchild2Path)), getAllDescendentPaths(templatePath, false, false));
        assertEquals(new HashSet<String>(asList(grandchild1Path, grandchild2Path)), getAllDescendentPaths(templatePath, true, true));
        assertEquals(new HashSet<String>(asList(grandchild1Path, grandchild2Path)), getAllDescendentPaths(templatePath, false, true));
        
        assertEquals(new HashSet<String>(), getAllDescendentPaths(childPath, true, false));
        assertEquals(new HashSet<String>(asList(childPath)), getAllDescendentPaths(childPath, false, false));
        assertEquals(new HashSet<String>(), getAllDescendentPaths(childPath, true, true));
        assertEquals(new HashSet<String>(asList(childPath)), getAllDescendentPaths(childPath, false, true));
    }

    private String insertConcreteChild(long parentHandle, String name) throws TypeException
    {
        MutableRecord record = unstantiate(new A(name));
        configurationTemplateManager.setParentTemplate(record, parentHandle);
        return configurationTemplateManager.insertRecord(SCOPE_TEMPLATE, record);
    }

    private Set<String> getAllDescendentPaths(String path, boolean strict, boolean concreteOnly)
    {
        Set<A> instances = configurationProvider.getAllDescendents(path, A.class, strict, concreteOnly);
        return CollectionUtils.map(instances, new Mapping<A, String>()
        {
            public String map(A a)
            {
                return a.getConfigurationPath();
            }
        }, new HashSet<String>());
    }

    @SymbolicName("a")
    public static class A extends AbstractConfiguration
    {
        @ID
        String field;
        B b;
        List<B> collectionB = new LinkedList<B>();

        public A(){}
        public A(String field){this.field = field;}
        public String getField(){return field;}
        public void setField(String field){this.field = field;}
        public B getB(){return b;}
        public void setB(B b){this.b = b;}
        public List<B> getCollectionB(){return collectionB;}
        public void setCollectionB(List<B> collectionB){this.collectionB = collectionB;}
    }

    @SymbolicName("b")
    public static class B extends AbstractConfiguration
    {
        String field;
        C c;
        List<C> collectionC = new LinkedList<C>();

        public B(){}
        public B(String field){this.field = field;}
        public String getField(){return field;}
        public void setField(String field){this.field = field;}
        public C getC() {return c;}
        public void setC(C c){this.c = c;}
        public List<C> getCollectionC(){return collectionC;}
        public void setCollectionC(List<C> collectionC){this.collectionC = collectionC;}
    }

    @SymbolicName("c")
    public static class C extends AbstractConfiguration
    {
        String field;
        public C(){}
        public C(String field){this.field = field;}
        public String getField(){return field;}
        public void setField(String field){this.field = field;}
    }
}
