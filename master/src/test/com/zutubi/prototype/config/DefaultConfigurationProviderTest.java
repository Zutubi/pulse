package com.zutubi.prototype.config;

import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.events.*;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.pulse.core.config.AbstractConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
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
        provider.init();
        
        typeRegistry.register(A.class);
        
        C c = new C("c");
        B b = new B("b");
        a = new A("a");
        a.setB(b);
        b.setC(c);

        CompositeType typeA = typeRegistry.getType(A.class);
        MapType mapA = new MapType(configurationTemplateManager);
        mapA.setTypeRegistry(typeRegistry);
        mapA.setCollectionType(typeA);

        configurationPersistenceManager.register("sample", mapA);
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
        configurationTemplateManager.insert("sample", a);
        listener.expected(PreInsertEvent.class, PostInsertEvent.class);
        listener.clear();

        a.setConfigurationPath("sample/a"); // should not be necessary...

        // check the save events.
        configurationTemplateManager.save("sample/a", a);
        listener.expected(PreSaveEvent.class, PostSaveEvent.class);
        listener.clear();

        // check the delete events.
        configurationTemplateManager.delete("sample/a");
        listener.expected(PreDeleteEvent.class, PostDeleteEvent.class);
        listener.clear();
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
        configurationTemplateManager.insert("sample", a);
        includingChildren.expected(PreInsertEvent.class, PreInsertEvent.class, PostInsertEvent.class, PostInsertEvent.class);
        includingChildren.clear();
        excludingChildren.expected();
        excludingChildren.clear();

        B b = new B("b");

        configurationTemplateManager.insert("sample/a/b", b);
        includingChildren.expected(PreInsertEvent.class, PreInsertEvent.class, PostInsertEvent.class, PostInsertEvent.class);
        includingChildren.clear();
        excludingChildren.expected();
        excludingChildren.clear();

        // check the save events.
        configurationTemplateManager.save("sample/a", a);
        // including listening at "sample", will see everything that happens below it.
        includingChildren.expected(PreSaveEvent.class, PostSaveEvent.class);
        includingChildren.clear();
        // excludingChildren listening at "sample", will not see changes to "sample/a"
        excludingChildren.expected();
        excludingChildren.clear();

        configurationTemplateManager.save("sample/a/b", b);
        includingChildren.expected(PreSaveEvent.class, PostSaveEvent.class);
        includingChildren.clear();
        excludingChildren.expected();
        excludingChildren.clear();

        // check the delete events.
        configurationTemplateManager.delete("sample/a");
        includingChildren.expected(PreDeleteEvent.class, PreDeleteEvent.class, PreDeleteEvent.class, PreDeleteEvent.class, PostDeleteEvent.class, PostDeleteEvent.class, PostDeleteEvent.class, PostDeleteEvent.class);
        includingChildren.clear();
        // excludingChildren listenening at "sample", will not see changes to "sample/a"
        excludingChildren.expected();
        excludingChildren.clear();
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
        listener.expected(PreInsertEvent.class, PostInsertEvent.class);
        listener.clear();

        configurationTemplateManager.delete("sample/a");
        listener.expected(PreDeleteEvent.class, PostDeleteEvent.class);
        listener.clear();
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
