package com.zutubi.prototype.config;

import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.events.PostDeleteEvent;
import com.zutubi.prototype.config.events.PostInsertEvent;
import com.zutubi.prototype.config.events.PostSaveEvent;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.TemplatedMapType;
import com.zutubi.pulse.core.config.AbstractConfiguration;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 */
public class DefaultConfigurationProviderTest extends AbstractConfigurationSystemTestCase
{
    private DefaultConfigurationProvider provider;

    private A a;

    protected void setUp() throws Exception
    {
        super.setUp();

        provider = new DefaultConfigurationProvider();
        provider.setEventManager(eventManager);
        provider.setTypeRegistry(typeRegistry);
        provider.setConfigurationPersistenceManager(configurationPersistenceManager);
        provider.setThreadFactory(Executors.defaultThreadFactory());
        provider.init();
        
        typeRegistry.register(A.class);
        
        C c = new C("c");
        B b = new B("b");
        a = new A("a");
        a.setB(b);
        b.setC(c);

        CompositeType typeA = typeRegistry.getType(A.class);
        MapType mapA = new MapType(typeA, typeRegistry);
        configurationPersistenceManager.register("sample", mapA);

        MapType templatedMap = new TemplatedMapType(typeA, typeRegistry);
        configurationPersistenceManager.register("template", templatedMap);
    }

    protected void tearDown() throws Exception
    {
        a = null;
        provider = null;

        super.tearDown();
    }

    public void testRegisterListenerByClass()
    {
        // check that we receive the save events.
        MockConfigurationEventListener listener = new MockConfigurationEventListener();
        provider.registerEventListener(listener, true, A.class);

        A a = new A("a");

        // check the insert events.
        String path = configurationTemplateManager.insert("sample", a);
        listener.assertNextEvent(PostInsertEvent.class, "sample/a");
        listener.assertNoMoreEvents();

        // check the save events.
        a = configurationTemplateManager.getCloneOfInstance(path, A.class);
        a.setField("edited");
        configurationTemplateManager.save(a);
        listener.assertNextEvent(PostSaveEvent.class, "sample/edited");
        listener.assertNoMoreEvents();

        // check the delete events.
        configurationTemplateManager.delete("sample/edited");
        listener.assertNextEvent(PostDeleteEvent.class, "sample/edited");
        listener.assertNoMoreEvents();
    }

    public void testRegisterListenerByPath()
    {
        // check that we receive the save events.
        MockConfigurationEventListener includingChildren = new MockConfigurationEventListener();
        provider.registerEventListener(includingChildren, true, true, "sample");

        MockConfigurationEventListener excludingChildren = new MockConfigurationEventListener();
        provider.registerEventListener(excludingChildren, true, false, "sample");

        A a = new A("a");

        // check the insert events.
        String aPath = configurationTemplateManager.insert("sample", a);
        includingChildren.assertNextEvent(PostInsertEvent.class, "sample/a");
        includingChildren.assertNoMoreEvents();
        excludingChildren.assertNoMoreEvents();

        configurationTemplateManager.insert("sample/a/b", new B("b"));
        includingChildren.assertNextEvent(PostInsertEvent.class, "sample/a/b");
        includingChildren.assertNoMoreEvents();
        excludingChildren.assertNoMoreEvents();

        // check the save events.
        a = configurationTemplateManager.getCloneOfInstance(aPath, A.class);
        a.setField("edited");
        configurationTemplateManager.save(a);
        // including listening at "sample", will see everything that happens below it.
        includingChildren.assertNextEvent(PostSaveEvent.class, "sample/edited");
        includingChildren.assertNoMoreEvents();
        // excludingChildren listening at "sample", will not see changes to "sample/a"
        excludingChildren.assertNoMoreEvents();

        B b = configurationTemplateManager.getCloneOfInstance("sample/edited/b", B.class);
        b.setField("edited");
        configurationTemplateManager.save(b);
        includingChildren.assertNextEvent(PostSaveEvent.class, "sample/edited/b");
        includingChildren.assertNoMoreEvents();
        excludingChildren.assertNoMoreEvents();

        // check the delete events.
        configurationTemplateManager.delete("sample/edited");
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
        configurationTemplateManager.insert("sample", a);
        listener.assertNextEvent(PostInsertEvent.class, "sample/a/b");
        listener.assertNoMoreEvents();

        configurationTemplateManager.delete("sample/a");
        listener.assertNextEvent(PostDeleteEvent.class, "sample/a/b");
        listener.assertNoMoreEvents();
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
