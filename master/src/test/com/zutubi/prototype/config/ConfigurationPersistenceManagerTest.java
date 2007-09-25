package com.zutubi.prototype.config;

import com.zutubi.prototype.config.types.CircularObject;
import com.zutubi.prototype.config.types.CompositeCollectionObject;
import com.zutubi.prototype.config.types.CompositeObject;
import com.zutubi.prototype.config.types.SimpleCollectionObject;
import com.zutubi.prototype.config.types.SimpleObject;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.store.InMemoryRecordStore;
import com.zutubi.prototype.transaction.TransactionManager;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 */
public class ConfigurationPersistenceManagerTest extends TestCase
{
    private ConfigurationPersistenceManager manager = null;
    private TypeRegistry typeRegistry = null;
    private RecordManager recordManager = null;
    
    private TransactionManager transactionManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeRegistry = new TypeRegistry();
        recordManager = new RecordManager();
        typeRegistry.setHandleAllocator(recordManager);
        
        transactionManager = new TransactionManager();

        InMemoryRecordStore inMemory = new InMemoryRecordStore();
        inMemory.setTransactionManager(transactionManager);

        recordManager = new RecordManager();
        recordManager.setTransactionManager(transactionManager);
        recordManager.setRecordStore(inMemory);
        recordManager.init();

        manager = new ConfigurationPersistenceManager();
        manager.setTypeRegistry(typeRegistry);
        manager.setRecordManager(recordManager);

        transactionManager.begin();
    }

    protected void tearDown() throws Exception
    {
        transactionManager = null;
        recordManager = null;
        typeRegistry = null;
        manager = null;

        super.tearDown();
    }

    public void testIndexSimple() throws TypeException
    {
        CompositeType type = typeRegistry.register(SimpleObject.class);
        manager.register("simple", type);
        assertEquals(Arrays.asList("simple"), manager.getConfigurationPaths(type));
    }

    public void testIndexComposite() throws TypeException
    {
        CompositeType type = typeRegistry.register(CompositeObject.class);
        manager.register("composite", type);
        assertEquals(Arrays.asList("composite"), manager.getConfigurationPaths(type));
        assertEquals(Arrays.asList("composite/simple", "composite/map/*"), manager.getConfigurationPaths(typeRegistry.getType("Simple")));
    }

    public void testIndexSimpleCollection() throws TypeException
    {
        CompositeType type = typeRegistry.register(SimpleCollectionObject.class);
        manager.register("collection", type);
        assertEquals(Arrays.asList("collection"), manager.getConfigurationPaths(type));
        assertEquals(Arrays.asList("collection/simpleList/*"), manager.getConfigurationPaths(typeRegistry.getType("Simple")));
    }

    public void testIndexCompositeCollection() throws TypeException
    {
        CompositeType type = typeRegistry.register(CompositeCollectionObject.class);
        manager.register("collection", type);
        assertEquals(Arrays.asList("collection"), manager.getConfigurationPaths(type));
        assertEquals(Arrays.asList("collection/composites/*"), manager.getConfigurationPaths(typeRegistry.getType("Composite")));
        assertEquals(Arrays.asList("collection/composites/*/simple", "collection/composites/*/map/*"), manager.getConfigurationPaths(typeRegistry.getType("Simple")));
    }

    public void testIndexTopLevelCollection() throws TypeException
    {
        CompositeType simple = typeRegistry.register(SimpleObject.class);
        MapType top = new MapType();
        top.setCollectionType(simple);
        manager.register("top", top);
        assertEquals(Arrays.asList("top/*"), manager.getConfigurationPaths(simple));
    }

    public void testIndexCircular() throws TypeException
    {
        try
        {
            CompositeType type = typeRegistry.register(CircularObject.class);
            manager.register("nested", type);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Cycle detected in type definition at path 'nested/nested': type 'com.zutubi.prototype.config.types.CircularObject' has already appeared in this path", e.getMessage());
        }
    }

}
