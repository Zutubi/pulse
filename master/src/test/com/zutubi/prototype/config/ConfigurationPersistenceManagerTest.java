package com.zutubi.prototype.config;

import com.zutubi.prototype.config.types.*;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MockRecordSerialiser;
import com.zutubi.prototype.type.record.RecordManager;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 */
public class ConfigurationPersistenceManagerTest extends TestCase
{
    private ConfigurationPersistenceManager manager = null;
    private TypeRegistry typeRegistry = null;
    private RecordManager recordManager = null;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeRegistry = new TypeRegistry();
        recordManager = new RecordManager();
        recordManager.setRecordSerialiser(new MockRecordSerialiser());
        manager = new ConfigurationPersistenceManager();
        manager.setTypeRegistry(typeRegistry);
        manager.setRecordManager(recordManager);
    }

    protected void tearDown() throws Exception
    {
        recordManager = null;
        typeRegistry = null;
        manager = null;

        super.tearDown();
    }

    public void testListingSimpleObject() throws TypeException
    {
        manager.register("simple", typeRegistry.register(SimpleObject.class));
        assertEquals(0, manager.getListing("simple").size());

        Type type = manager.getType("simple");
        assertEquals(SimpleObject.class, type.getClazz());
    }

    public void testListingCollectionObject() throws TypeException
    {
        manager.register("simpleCollection", typeRegistry.register(SimpleCollectionObject.class));
        assertEquals(1, manager.getListing("simpleCollection").size());
        assertEquals(0, manager.getListing("simpleCollection/simpleList").size());
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
        assertEquals(Arrays.asList("composite/simple"), manager.getConfigurationPaths(typeRegistry.getType("Simple")));
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
        assertEquals(Arrays.asList("collection/composites/*/simple"), manager.getConfigurationPaths(typeRegistry.getType("Simple")));
    }

    public void testIndexTopLevelCollection() throws TypeException
    {
        CompositeType simple = typeRegistry.register(SimpleObject.class);
        TopLevelMapType top = new TopLevelMapType();
        top.setCollectionType(simple);
        manager.register("top", top);
        assertEquals(Arrays.asList("top/*"), manager.getConfigurationPaths(simple));
    }

    public void testIndexCircular() throws TypeException
    {
        // FIXME make nested work
//        CompositeType type = typeRegistry.register(CircularObject.class);
//        manager.register("nested", type);
//        assertEquals(Arrays.asList("nested", "nested/nested"), manager.getConfigurationPaths(type));
    }

}
