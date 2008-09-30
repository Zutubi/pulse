package com.zutubi.tove.config;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.security.GlobalAuthorityProvider;
import com.zutubi.pulse.security.PulseThreadFactory;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.tove.config.cleanup.ConfigurationCleanupManager;
import com.zutubi.tove.security.Actor;
import com.zutubi.tove.security.ActorProvider;
import com.zutubi.tove.security.DefaultAccessManager;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.store.InMemoryRecordStore;
import com.zutubi.util.bean.WiringObjectFactory;
import com.zutubi.validation.DefaultValidationManager;
import com.zutubi.validation.ValidatorProvider;
import com.zutubi.validation.providers.AnnotationValidatorProvider;
import com.zutubi.validation.providers.ReflectionValidatorProvider;

import java.util.Arrays;

/**
 *
 *
 */
public abstract class AbstractConfigurationSystemTestCase extends PulseTestCase
{
    protected WiringObjectFactory objectFactory = new WiringObjectFactory();
    protected DefaultValidationManager validationManager;
    protected TypeRegistry typeRegistry;
    protected DefaultEventManager eventManager;
    protected RecordManager recordManager;
    protected ConfigurationPersistenceManager configurationPersistenceManager;
    protected ConfigurationTemplateManager configurationTemplateManager;
    protected ConfigurationReferenceManager configurationReferenceManager;
    protected ConfigurationSecurityManager configurationSecurityManager;
    protected ConfigurationCleanupManager configurationCleanupManager;
    protected ConfigurationStateManager configurationStateManager;
    protected TransactionManager transactionManager;
    protected DefaultAccessManager accessManager;
    private PulseThreadFactory threadFactory;
    protected DefaultConfigurationProvider configurationProvider = null;

    protected void setUp() throws Exception
    {
        super.setUp();

        validationManager = new DefaultValidationManager();
        ConfigurationValidatorProvider validatorProvider = new ConfigurationValidatorProvider();
        ReflectionValidatorProvider reflectionProvider = new ReflectionValidatorProvider();
        AnnotationValidatorProvider annotationProvider = new AnnotationValidatorProvider();
        annotationProvider.setObjectFactory(objectFactory);
        validatorProvider.setDelegates(Arrays.asList(reflectionProvider, annotationProvider));
        validationManager.setProviders(Arrays.asList((ValidatorProvider) validatorProvider));

        threadFactory = new PulseThreadFactory();
        typeRegistry = new TypeRegistry();
        eventManager = new DefaultEventManager();

        transactionManager = new TransactionManager();

        InMemoryRecordStore inMemory = new InMemoryRecordStore();
        inMemory.setTransactionManager(transactionManager);

        recordManager = new RecordManager();
        recordManager.setTransactionManager(transactionManager);
        recordManager.setRecordStore(inMemory);
        recordManager.init();

        accessManager = new DefaultAccessManager();
        accessManager.setActorProvider(new ActorProvider()
        {
            public Actor getActor()
            {
                return AcegiUtils.getSystemUser();
            }
        });
        new GlobalAuthorityProvider().setAccessManager(accessManager);

        configurationPersistenceManager = new ConfigurationPersistenceManager();
        configurationPersistenceManager.setTypeRegistry(typeRegistry);
        configurationPersistenceManager.setRecordManager(recordManager);

        configurationTemplateManager = new ConfigurationTemplateManager();
        configurationTemplateManager.setEventManager(eventManager);
        configurationTemplateManager.setRecordManager(recordManager);
        configurationTemplateManager.setTypeRegistry(typeRegistry);
        configurationTemplateManager.setConfigurationPersistenceManager(configurationPersistenceManager);
        configurationTemplateManager.setValidationManager(validationManager);
        configurationTemplateManager.setTransactionManager(transactionManager);
        
        configurationReferenceManager = new ConfigurationReferenceManager();
        configurationReferenceManager.setRecordManager(recordManager);
        configurationReferenceManager.setTypeRegistry(typeRegistry);
        configurationReferenceManager.setObjectFactory(objectFactory);
        configurationReferenceManager.setConfigurationTemplateManager(configurationTemplateManager);
        configurationReferenceManager.setConfigurationPersistenceManager(configurationPersistenceManager);

        configurationSecurityManager = new ConfigurationSecurityManager();
        configurationSecurityManager.setAccessManager(accessManager);
        configurationSecurityManager.setConfigurationTemplateManager(configurationTemplateManager);
        configurationSecurityManager.setEventManager(eventManager);

        configurationCleanupManager = new ConfigurationCleanupManager();
        configurationCleanupManager.setObjectFactory(objectFactory);
        configurationCleanupManager.setEventManager(eventManager);

        configurationStateManager = new ConfigurationStateManager();
        configurationStateManager.setRecordManager(recordManager);
        configurationStateManager.setTransactionManager(transactionManager);
        configurationStateManager.setTypeRegistry(typeRegistry);

        configurationTemplateManager.setConfigurationReferenceManager(configurationReferenceManager);
        configurationTemplateManager.setConfigurationSecurityManager(configurationSecurityManager);
        configurationTemplateManager.setConfigurationCleanupManager(configurationCleanupManager);
        configurationTemplateManager.setConfigurationStateManager(configurationStateManager);

        configurationProvider = new DefaultConfigurationProvider();
        configurationProvider.setEventManager(eventManager);
        configurationProvider.setTypeRegistry(typeRegistry);
        configurationProvider.setConfigurationPersistenceManager(configurationPersistenceManager);
        configurationProvider.setConfigurationTemplateManager(configurationTemplateManager);
        configurationProvider.setConfigurationStateManager(configurationStateManager);
        configurationProvider.setThreadFactory(threadFactory);

        configurationTemplateManager.init();
        configurationProvider.init();

        typeRegistry.setConfigurationReferenceManager(configurationReferenceManager);
        typeRegistry.setHandleAllocator(recordManager);

        objectFactory.initProperties(this);
    }

    protected void tearDown() throws Exception
    {
        configurationTemplateManager = null;
        typeRegistry = null;
        eventManager = null;
        recordManager = null;
        configurationReferenceManager = null;
        configurationPersistenceManager = null;
        configurationStateManager = null;

        super.tearDown();
    }

    public MutableRecord unstantiate(Configuration c) throws TypeException
    {
        CompositeType type = typeRegistry.getType(c.getClass());
        return type.unstantiate(c);
    }
}
