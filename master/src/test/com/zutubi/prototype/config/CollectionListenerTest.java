package com.zutubi.prototype.config;

import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.config.Configuration;

/**
 *
 *
 */
public class CollectionListenerTest extends AbstractConfigurationSystemTestCase
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
        MockCollectionListener listener = new MockCollectionListener<A>("sample", A.class, true);
        listener.register(provider);

        A a = new A("a");

        assertNull(listener.preInsertRecord);
        assertNull(listener.insertedInstance);
        configurationTemplateManager.insert("sample", a);
        assertNotNull(listener.preInsertRecord);
        assertNotNull(listener.insertedInstance);

        assertTrue(listener.insertedInstance instanceof A);
        assertEquals(a.getStr(), ((A)listener.insertedInstance).getStr());
        
    }

    public void testInstanceChanged()
    {
        MockCollectionListener listener = new MockCollectionListener<A>("sample", A.class, true);
        listener.register(provider);

        A a = new A("a");

        configurationTemplateManager.insert("sample", a);

        a.setStr("b");

        assertNull(listener.changedInstance);
        configurationTemplateManager.save("sample/a", a);
        assertNotNull(listener.changedInstance);

        assertTrue(listener.changedInstance instanceof A);
        assertEquals(a.getStr(), ((A)listener.changedInstance).getStr());
    }

    public void testInstanceDeleted()
    {
        MockCollectionListener listener = new MockCollectionListener<A>("sample", A.class, true);
        listener.register(provider);

        A a = new A("a");

        configurationTemplateManager.insert("sample", a);

        assertNull(listener.deletedInstance);
        configurationTemplateManager.delete("sample/a");
        assertNotNull(listener.deletedInstance);

        assertTrue(listener.deletedInstance instanceof A);
        assertEquals(a.getStr(), ((A)listener.deletedInstance).getStr());
    }

    public void testNestedChangeTriggeredInstanceChanged()
    {
        MockCollectionListener listener = new MockCollectionListener<A>("sample", A.class, true);
        listener.register(provider);

        A a = new A("a");
        B b = new B("b");
        a.setB(b);

        configurationTemplateManager.insert("sample", a);

        b.setStr("c");

        assertNull(listener.changedInstance);
        configurationTemplateManager.save("sample/a/b", b);
        assertNotNull(listener.changedInstance);

    }

    private class MockCollectionListener<X extends Configuration> extends CollectionListener<X>
    {
        public Record preInsertRecord;
        public Configuration insertedInstance;
        public Configuration deletedInstance;
        public Configuration changedInstance;

        public MockCollectionListener(String path, Class<X> configurationClass, boolean synchronous)
        {
            super(path, configurationClass, synchronous);
        }

        protected void preInsert(MutableRecord record)
        {
            preInsertRecord = record;
        }

        protected void instanceInserted(Configuration instance)
        {
            insertedInstance = instance;
        }

        protected void instanceDeleted(Configuration instance)
        {
            deletedInstance = instance;
        }

        protected void instanceChanged(Configuration instance)
        {
            changedInstance = instance;
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
