/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.transaction.TransactionResource;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the mapping from types to external state managers, and the creation of state.
 */
public class ConfigurationStateManager
{
    private static final Logger LOG = Logger.getLogger(ConfigurationStateManager.class);

    private Map<Class<? extends Configuration>, ExternalStateManager<?>> managers = new HashMap<>();

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
        return new ArrayList<>(managers.keySet());
    }

    /**
     * Creates state matching the given instance and assigns the new id to that instance's external
     * state property.
     * 
     * @param instance the instance to create state for
     * @param force true to create the state even if the instance already has a non-zero id in its
     *              external state property
     */
    public void createAndAssignState(final Configuration instance, final boolean force)
    {
        Class<? extends Configuration> clazz = instance.getClass();
        final ExternalStateManager<Configuration> manager = getManager(clazz);
        if (manager != null)
        {
            if (force || getExternalStateId(instance) == 0)
            {
                final long[] hax = {-1};
                transactionManager.runInTransaction(new Runnable()
                {
                    public void run()
                    {
                        CompositeType type = typeRegistry.getType(instance.getClass());

                        hax[0] = manager.createState(instance);
                        updateProperty(type, instance, hax[0]);
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
    }

    @SuppressWarnings("unchecked")
    private ExternalStateManager<Configuration> getManager(Class<? extends Configuration> clazz)
    {
        ExternalStateManager<Configuration> manager = (ExternalStateManager<Configuration>) managers.get(clazz);
        if(manager == null)
        {
            Class<?> superClazz = clazz.getSuperclass();
            if(superClazz != null  && Configuration.class.isAssignableFrom(superClazz))
            {
                manager = getManager((Class<? extends Configuration>) superClazz);
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
        catch (Exception e)
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
