package com.zutubi.tove.config;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.Event;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.cleanup.ConfigurationCleanupManager;
import com.zutubi.tove.config.events.*;
import com.zutubi.tove.security.Actor;
import com.zutubi.tove.security.ActorProvider;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;
import com.zutubi.tove.transaction.AbstractTransactionTestCase;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.store.InMemoryRecordStore;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.bean.WiringObjectFactory;
import com.zutubi.validation.DefaultValidationManager;
import com.zutubi.validation.ValidatorProvider;
import com.zutubi.validation.providers.AnnotationValidatorProvider;
import com.zutubi.validation.providers.ReflectionValidatorProvider;

import java.util.*;
import java.util.concurrent.ThreadFactory;

public abstract class AbstractConfigurationSystemTestCase extends AbstractTransactionTestCase
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

        typeRegistry = new TypeRegistry();
        eventManager = new DefaultEventManager();

        transactionManager = new TransactionManager();

        InMemoryRecordStore inMemory = new InMemoryRecordStore();
        inMemory.setTransactionManager(transactionManager);

        recordManager = new RecordManager();
        recordManager.setTransactionManager(transactionManager);
        recordManager.setRecordStore(inMemory);
        recordManager.setEventManager(eventManager);
        recordManager.init();

        // emulate administer auths.
        accessManager = new DefaultAccessManager();
        accessManager.setActorProvider(new ActorProvider()
        {
            public Actor getActor()
            {
                return new Actor()
                {
                    public String getUsername()
                    {
                        return null;
                    }

                    public Set<String> getGrantedAuthorities()
                    {
                        Set<String> auths = new HashSet<String>();
                        auths.add("ADMINISTER");
                        return auths;
                    }

                    public boolean isAnonymous()
                    {
                        return false;
                    }
                };
            }
        });
        accessManager.addSuperAuthority("ADMINISTER");
        accessManager.registerAuthorityProvider(new AuthorityProvider<Object>()
        {
            public Set<String> getAllowedAuthorities(String action, Object resource)
            {
                Set<String> auths = new HashSet<String>();
                auths.add(action);
                return auths;
            }
        });


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
        configurationProvider.setThreadFactory(new ThreadFactory()
        {
            public Thread newThread(Runnable r)
            {
                return new Thread(r);
            }
        });
        configurationTemplateManager.init();
        configurationProvider.init();

        typeRegistry.setConfigurationReferenceManager(configurationReferenceManager);
        typeRegistry.setHandleAllocator(recordManager);

        objectFactory.initProperties(this);
    }

    public MutableRecord unstantiate(Configuration c) throws TypeException
    {
        CompositeType type = typeRegistry.getType(c.getClass());
        return type.unstantiate(c);
    }

    protected Listener registerListener()
    {
        Listener listener = new Listener();
        eventManager.register(listener);
        return listener;
    }

    public static class Listener implements com.zutubi.events.EventListener
    {
        private List<ConfigurationEvent> events = new LinkedList<ConfigurationEvent>();

        public List<ConfigurationEvent> getEvents()
        {
            return events;
        }

        public void clearEvents()
        {
            events.clear();
        }

        public void assertEvents(TemplateRecordPersistenceTest.EventSpec... expectedEvents)
        {
            assertEquals(expectedEvents.length, events.size());
            for(final TemplateRecordPersistenceTest.EventSpec spec: expectedEvents)
            {
                ConfigurationEvent matchingEvent = CollectionUtils.find(events, new Predicate<ConfigurationEvent>()
                {
                    public boolean satisfied(ConfigurationEvent event)
                    {
                        return spec.matches(event);
                    }
                });

                assertNotNull("Expected event '" + spec.toString() + "' missing", matchingEvent);
            }
        }

        public void handleEvent(Event evt)
        {
            events.add((ConfigurationEvent) evt);
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{ConfigurationEvent.class};
        }
    }

    public static abstract class EventSpec
    {
        private String path;
        private boolean cascaded;
        private Class<? extends ConfigurationEvent> eventClass;

        protected EventSpec(String path, Class<? extends ConfigurationEvent> eventClass)
        {
            this(path, false, eventClass);
        }

        protected EventSpec(String path, boolean cascaded, Class<? extends ConfigurationEvent> eventClass)
        {
            this.path = path;
            this.cascaded = cascaded;
            this.eventClass = eventClass;
        }

        public boolean matches(ConfigurationEvent event)
        {
            if(event instanceof CascadableEvent)
            {
                if(cascaded != ((CascadableEvent)event).isCascaded())
                {
                    return false;
                }
            }

            return eventClass.isInstance(event) && event.getInstance().getConfigurationPath().equals(path);
        }

        public String toString()
        {
            return eventClass.getSimpleName() + ": " + path;
        }
    }

    public static class InsertEventSpec extends EventSpec
    {
        public InsertEventSpec(String path, boolean cascaded)
        {
            super(path, cascaded, InsertEvent.class);
        }
    }

    public static class DeleteEventSpec extends EventSpec
    {
        public DeleteEventSpec(String path, boolean cascaded)
        {
            super(path, cascaded, DeleteEvent.class);
        }
    }

    public static class SaveEventSpec extends EventSpec
    {
        public SaveEventSpec(String path)
        {
            super(path, SaveEvent.class);
        }
    }

    public static class PostInsertEventSpec extends EventSpec
    {
        public PostInsertEventSpec(String path, boolean cascaded)
        {
            super(path, cascaded, PostInsertEvent.class);
        }
    }

    public static class PostDeleteEventSpec extends EventSpec
    {
        public PostDeleteEventSpec(String path, boolean cascaded)
        {
            super(path, cascaded, PostDeleteEvent.class);
        }
    }

    public static class PostSaveEventSpec extends EventSpec
    {
        public PostSaveEventSpec(String path)
        {
            super(path, PostSaveEvent.class);
        }
    }
}
