package com.zutubi.prototype.config;

import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MockRecordSerialiser;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.events.DefaultEventManager;
import com.zutubi.pulse.validation.PulseValidationManager;
import com.zutubi.validation.ValidatorProvider;
import com.zutubi.validation.providers.AnnotationValidatorProvider;
import com.zutubi.validation.providers.ReflectionValidatorProvider;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 *
 *
 */
public abstract class AbstractConfigurationSystemTestCase extends TestCase
{
    protected PulseValidationManager validationManager;
    protected TypeRegistry typeRegistry;
    protected DefaultEventManager eventManager;
    protected RecordManager recordManager;
    protected ConfigurationPersistenceManager configurationPersistenceManager;
    protected ConfigurationTemplateManager configurationTemplateManager;
    protected ConfigurationReferenceManager configurationReferenceManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        validationManager = new PulseValidationManager();
        ConfigurationValidatorProvider validatorProvider = new ConfigurationValidatorProvider();
        validatorProvider.setDelegates(Arrays.asList(new ReflectionValidatorProvider(), new AnnotationValidatorProvider()));
        validationManager.setProviders(Arrays.asList((ValidatorProvider) validatorProvider));

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
        configurationTemplateManager.setValidationManager(validationManager);
        
        configurationReferenceManager = new ConfigurationReferenceManager();
        configurationReferenceManager.setRecordManager(recordManager);
        configurationReferenceManager.setTypeRegistry(typeRegistry);
        configurationReferenceManager.setConfigurationTemplateManager(configurationTemplateManager);
        configurationReferenceManager.setConfigurationPersistenceManager(configurationPersistenceManager);

        configurationTemplateManager.setConfigurationReferenceManager(configurationReferenceManager);

        typeRegistry.setConfigurationReferenceManager(configurationReferenceManager);
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
