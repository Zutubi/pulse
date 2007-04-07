package com.zutubi.prototype.config;

import junit.framework.TestCase;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.MockRecordSerialiser;
import com.zutubi.prototype.config.types.SimpleObject;
import com.zutubi.prototype.config.types.CompositeObject;
import com.zutubi.prototype.config.types.GrandParentObject;

/**
 */
public class ReferencableRecordsTest extends TestCase
{
    private TypeRegistry typeRegistry;
    private RecordManager recordManager;
    private ConfigurationPersistenceManager configurationPersistenceManager;

    private CompositeType simpleType;
    private CompositeType compositeType;
    private CompositeType grandparentType;

    protected void setUp() throws Exception
    {
        typeRegistry = new TypeRegistry();
        recordManager = new RecordManager();
        recordManager.setRecordSerialiser(new MockRecordSerialiser());
        configurationPersistenceManager = new ConfigurationPersistenceManager();
        configurationPersistenceManager.setTypeRegistry(typeRegistry);
        configurationPersistenceManager.setRecordManager(recordManager);

        simpleType = typeRegistry.register(SimpleObject.class);
        compositeType = typeRegistry.register(CompositeObject.class);
        grandparentType = typeRegistry.register(GrandParentObject.class);
    }
}
