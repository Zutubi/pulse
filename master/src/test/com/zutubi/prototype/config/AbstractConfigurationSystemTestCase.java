package com.zutubi.prototype.config;

import com.zutubi.prototype.security.Actor;
import com.zutubi.prototype.security.ActorProvider;
import com.zutubi.prototype.security.DefaultAccessManager;
import com.zutubi.prototype.transaction.TransactionManager;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.store.InMemoryRecordStore;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.events.DefaultEventManager;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.security.GlobalAuthorityProvider;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.validation.DefaultValidationManager;
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
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected DefaultValidationManager validationManager;
    protected TypeRegistry typeRegistry;
    protected DefaultEventManager eventManager;
    protected RecordManager recordManager;
    protected ConfigurationPersistenceManager configurationPersistenceManager;
    protected ConfigurationTemplateManager configurationTemplateManager;
    protected ConfigurationReferenceManager configurationReferenceManager;
    protected ConfigurationSecurityManager configurationSecurityManager;
    protected TransactionManager transactionManager;
    protected DefaultAccessManager accessManager;

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
        
        configurationReferenceManager = new ConfigurationReferenceManager();
        configurationReferenceManager.setRecordManager(recordManager);
        configurationReferenceManager.setTypeRegistry(typeRegistry);
        configurationReferenceManager.setConfigurationTemplateManager(configurationTemplateManager);
        configurationReferenceManager.setConfigurationPersistenceManager(configurationPersistenceManager);

        configurationSecurityManager = new ConfigurationSecurityManager();
        configurationSecurityManager.setAccessManager(accessManager);
        configurationSecurityManager.setConfigurationTemplateManager(configurationTemplateManager);

        configurationTemplateManager.setConfigurationReferenceManager(configurationReferenceManager);
        configurationTemplateManager.setConfigurationSecurityManager(configurationSecurityManager);

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

    public MutableRecord unstantiate(Configuration c) throws TypeException
    {
        CompositeType type = typeRegistry.getType(c.getClass());
        return type.unstantiate(c);
    }
}
