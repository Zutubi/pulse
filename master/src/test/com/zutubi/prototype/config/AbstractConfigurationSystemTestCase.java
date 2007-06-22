package com.zutubi.prototype.config;

import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MockRecordSerialiser;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.events.DefaultEventManager;
import junit.framework.TestCase;

/**
 *
 *
 */
public abstract class AbstractConfigurationSystemTestCase extends TestCase
{
    protected ConfigurationTemplateManager configurationTemplateManager;
    protected TypeRegistry typeRegistry;
    protected DefaultEventManager eventManager;
    protected RecordManager recordManager;
    protected ConfigurationPersistenceManager configurationPersistenceManager;
    protected ConfigurationReferenceManager configurationReferenceManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeRegistry = new TypeRegistry();
        eventManager = new DefaultEventManager();

        recordManager = new RecordManager();
        recordManager.setRecordSerialiser(new MockRecordSerialiser());
        recordManager.init();

        configurationPersistenceManager = new ConfigurationPersistenceManager();
        configurationPersistenceManager.setTypeRegistry(typeRegistry);
        configurationPersistenceManager.setRecordManager(recordManager);

        configurationTemplateManager = new ConfigurationTemplateManager();
        configurationTemplateManager.setEventManager(eventManager);
        configurationTemplateManager.setRecordManager(recordManager);
        configurationTemplateManager.setTypeRegistry(typeRegistry);
        configurationTemplateManager.setConfigurationPersistenceManager(configurationPersistenceManager);

        configurationReferenceManager = new ConfigurationReferenceManager();
        configurationReferenceManager.setRecordManager(recordManager);
        configurationReferenceManager.setTypeRegistry(typeRegistry);
        configurationReferenceManager.setConfigurationTemplateManager(configurationTemplateManager);
        configurationReferenceManager.setConfigurationPersistenceManager(configurationPersistenceManager);

        configurationTemplateManager.setConfigurationReferenceManager(configurationReferenceManager);

        typeRegistry.setConfigurationReferenceManager(configurationReferenceManager);
        typeRegistry.setConfigurationTemplateManager(configurationTemplateManager);
        typeRegistry.setHandleAllocator(recordManager);
    }

    protected void tearDown() throws Exception
    {
        configurationTemplateManager = null;
        typeRegistry = null;
        eventManager = null;
        recordManager = null;
        configurationReferenceManager = null;
        configurationPersistenceManager = null;

        super.tearDown();
    }
}
