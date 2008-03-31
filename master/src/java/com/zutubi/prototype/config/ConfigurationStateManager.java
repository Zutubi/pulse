package com.zutubi.prototype.config;

import com.zutubi.prototype.transaction.Transaction;
import com.zutubi.prototype.transaction.TransactionManager;
import com.zutubi.prototype.transaction.TransactionResource;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
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

    public void instanceInserted(Configuration instance)
    {
        Class<? extends Configuration> clazz = instance.getClass();
        final ExternalStateManager manager = getManager(clazz);
        if (manager != null)
        {
            CompositeType type = typeRegistry.getType(instance.getClass());
            Transaction txn = transactionManager.getTransaction();
            try
            {
                final long id = manager.createState(instance);

                // Make sure state is cleaned up if transaction fails.
                txn.enlistResource(new TransactionResource()
                {
                    public boolean prepare()
                    {
                        return true;
                    }

                    public void commit()
                    {
                        // Nothing to do.
                    }

                    public void rollback()
                    {
                        manager.rollbackState(id);
                    }
                });

                updateProperty(type, instance, id);
            }
            catch (Exception e)
            {
                LOG.severe(e);

                // Make sure the commit does not go ahead.
                txn.enlistResource(new TransactionResource()
                {
                    public boolean prepare()
                    {
                        return false;
                    }

                    public void commit()
                    {
                    }

                    public void rollback()
                    {
                    }
                });
            }

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
            MutableRecord mutable = record.copy(false);
            mutable.put(property.getName(), property.getType().unstantiate(id));
            recordManager.update(instance.getConfigurationPath(), mutable);
            property.setValue(instance, id);
        }
        catch (TypeException e)
        {
            LOG.severe(e);
        }
    }

//    public void instanceDelete(Configuration instance)
//    {
//        final ExternalStateManager manager = managers.get(instance.getClass());
//        if (manager != null)
//        {
//            CompositeType type = typeRegistry.getType(instance.getClass());
//            TypeProperty property = type.getExternalStateProperty();
//            try
//            {
//                final Long id = (Long) property.getValue(instance);
//                if (id != null)
//                {
//                    Transaction txn = transactionManager.getTransaction();
//                    txn.enlistResource(new TransactionResource()
//                    {
//                        public boolean prepare()
//                        {
//                            return true;
//                        }
//
//                        public void commit()
//                        {
//                            manager.cleanupState(id);
//                        }
//
//                        public void rollback()
//                        {
//                            // Do nothing.
//                        }
//                    });
//                }
//            }
//            catch (Exception e)
//            {
//                LOG.severe(e);
//            }
//        }
//    }

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
