package com.zutubi.prototype.config;

import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.config.Configuration;

/**
 *
 *
 */
public class TypeListenerTest extends AbstractConfigurationSystemTestCase
{
    private DefaultConfigurationProvider provider = null;

    protected void setUp() throws Exception
    {
        super.setUp();

        provider = new DefaultConfigurationProvider();
        provider.setEventManager(eventManager);
        provider.setTypeRegistry(typeRegistry);
        provider.setConfigurationPersistenceManager(configurationPersistenceManager);
        provider.setConfigurationTemplateManager(configurationTemplateManager);
        provider.init();

        CompositeType typeA = typeRegistry.register("a", A.class);
        MapType mapA = new MapType();
        mapA.setTypeRegistry(typeRegistry);
        mapA.setCollectionType(typeA);

        configurationPersistenceManager.register("sample", mapA);
    }

    protected void tearDown() throws Exception
    {
        provider = null;

        super.tearDown();
    }

    public void testInstanceInserted()
    {
        MockTypeListener<A> listener = new MockTypeListener<A>(A.class);
        listener.register(provider);

        A a = new A("a");
        
        assertNull(listener.insertedInstance);
        configurationTemplateManager.insert("sample", a);
        assertNotNull(listener.insertedInstance);
    }

    public void testInstanceChanged()
    {
        MockTypeListener<A> listener = new MockTypeListener<A>(A.class);
        listener.register(provider);

        A a = new A("a");

        configurationTemplateManager.insert("sample", a);
        a.setB(new B("b"));

        assertNull(listener.savedInstance);
        configurationTemplateManager.save("sample/a", a);
        assertNotNull(listener.savedInstance);
    }

    public void testInstanceDeleted()
    {
        MockTypeListener<A> listener = new MockTypeListener<A>(A.class);
        listener.register(provider);

        A a = new A("a");

        configurationTemplateManager.insert("sample", a);

        assertNull(listener.deletedInstance);
        configurationTemplateManager.delete("sample/a");
        assertNotNull(listener.deletedInstance);
    }

    public void testNestedChangeTriggeredInstanceChanged()
    {

    }

    private class MockTypeListener<X extends Configuration> extends TypeListener<X>
    {
        X insertedInstance;
        X savedInstance;
        X deletedInstance;

        public MockTypeListener(Class<X> configurationClass)
        {
            super(configurationClass);
        }

        public void postInsert(X instance)
        {
            insertedInstance = instance;
        }

        public void postDelete(X instance)
        {
            deletedInstance = instance;
        }

        public void postSave(X instance)
        {
            savedInstance = instance;
        }
    }


    @SymbolicName("a")
    public static class A extends AbstractConfiguration
    {
        @ID
        private String str;
        private B b;
        public A(){}
        public A(String a){this.str = a;}
        public String getStr(){return str;}
        public void setStr(String str){this.str = str;}
        public B getB(){return b;}
        public void setB(B b){this.b = b;}
    }

    @SymbolicName("b")
    public static class B extends AbstractConfiguration
    {
        @ID
        private String str;
        public B(){}
        public B(String str){this.str = str;}
        public String getStr(){return str;}
        public void setStr(String str){this.str = str;}
    }
}

