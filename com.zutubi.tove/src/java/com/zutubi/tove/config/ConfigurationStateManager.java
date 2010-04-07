package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.transaction.TransactionResource;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.NullaryFunction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class ConfigurationStateManager
{
    private static final Logger LOG = Logger.getLogger(ConfigurationStateManager.class);

    private Map<Class<? extends Configuration>, ExternalStateManager<?>> managers = new HashMap<Class<? extends Configuration>, ExternalStateManager<?>>();

    private TransactionManager transactionManager;
    private TypeRegistry typeRegistry;
    private RecordManager recordManager;

    public void register(Class<? extends Configuration> clazz, ExternalStateManager<?> manager)
    {
        CompositeType type = typeRegistry.getType(clazz);
        if (type == null)
        {
            throw new IllegalArgumentException("Invalid class '" + clazz.getName() + "': not registered");
        }

        if (type.getExternalStateProperty() == null)
        {
            throw new IllegalArgumentException("Invalid class '" + clazz.getName() + "': no external state property");
        }

        managers.put(clazz, manager);
    }

    public List<Class<? extends Configuration>> getStatefulConfigurationTypes()
    {
        return new LinkedList<Class<? extends Configuration>>(managers.keySet());
    }

    public void createAndAssignState(final Configuration instance)
    {
        Class<? extends Configuration> clazz = instance.getClass();
        final ExternalStateManager manager = getManager(clazz);
        if (manager != null)
        {
            final long[] hax = {-1};
            transactionManager.runInTransaction(new NullaryFunction()
            {
                public Object process()
                {
                    CompositeType type = typeRegistry.getType(instance.getClass());

                    hax[0] = manager.createState(instance);
                    updateProperty(type, instance, hax[0]);

                    return null;
                }

            },
            new TransactionResource()
            {
                public boolean prepare()
                {
                    return true;
                }

                public void commit()
                {

                }

                public void rollback()
                {
                    manager.rollbackState(hax[0]);
                }

            });
        }
    }

    private ExternalStateManager<?> getManager(Class<? extends Configuration> clazz)
    {
        ExternalStateManager<?> manager = managers.get(clazz);
        if(manager == null)
        {
            Class superClazz = clazz.getSuperclass();
            if(superClazz != null)
            {
                manager = getManager(superClazz);
                if(manager != null)
                {
                    // Cache for next time
                    managers.put(clazz, manager);
                }
            }
        }
        
        return manager;
    }

    private void updateProperty(CompositeType type, Configuration instance, long id)
    {
        try
        {
            TypeProperty property = type.getExternalStateProperty();
            Record record = recordManager.select(instance.getConfigurationPath());
            MutableRecord mutable = record.copy(false, true);
            mutable.put(property.getName(), property.getType().unstantiate(id, null));
            recordManager.update(instance.getConfigurationPath(), mutable);
            property.setValue(instance, id);
        }
        catch (TypeException e)
        {
            LOG.severe(e);
        }
    }

    public long getExternalStateId(Configuration instance)
    {
        CompositeType type = typeRegistry.getType(instance.getClass());
        TypeProperty property = type.getExternalStateProperty();
        if (property == null)
        {
            throw new IllegalArgumentException("Type " + instance.getClass() + " does not have an external state.");
        }
        try
        {
            Object value = property.getValue(instance);
            return (Long)value;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to retrieve external state id.");
        }
    }

    public Object getExternalState(Configuration instance)
    {
        long id = getExternalStateId(instance);
        ExternalStateManager stateManager = getManager(instance.getClass());
        return stateManager.getState(id);
    }

    public void setTransactionManager(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

}
