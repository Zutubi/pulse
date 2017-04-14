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

import com.zutubi.events.DefaultEventManager;
import com.zutubi.tove.config.types.*;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.store.InMemoryRecordStore;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Arrays;

public class ConfigurationPersistenceManagerTest extends ZutubiTestCase
{
    private ConfigurationPersistenceManager manager = null;
    private TypeRegistry typeRegistry = null;
    private RecordManager recordManager = null;

    private TransactionManager transactionManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeRegistry = new TypeRegistry();
        recordManager = new RecordManager();
        typeRegistry.setHandleAllocator(recordManager);

        transactionManager = new TransactionManager();

        InMemoryRecordStore inMemory = new InMemoryRecordStore();
        inMemory.setTransactionManager(transactionManager);
        inMemory.init();

        recordManager = new RecordManager();
        recordManager.setTransactionManager(transactionManager);
        recordManager.setRecordStore(inMemory);
        recordManager.setEventManager(new DefaultEventManager());
        recordManager.init();

        manager = new ConfigurationPersistenceManager();
        manager.setTypeRegistry(typeRegistry);
        manager.setRecordManager(recordManager);

        transactionManager.begin();
    }

    protected void tearDown() throws Exception
    {
        transactionManager = null;
        recordManager = null;
        typeRegistry = null;
        manager = null;

        super.tearDown();
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
        assertEquals(Arrays.asList("composite/simple", "composite/map/*"), manager.getConfigurationPaths(typeRegistry.getType("Simple")));
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
        assertEquals(Arrays.asList("collection/composites/*/simple", "collection/composites/*/map/*"), manager.getConfigurationPaths(typeRegistry.getType("Simple")));
    }

    public void testIndexTopLevelCollection() throws TypeException
    {
        CompositeType simple = typeRegistry.register(SimpleObject.class);
        MapType top = new MapType(simple, typeRegistry);
        manager.register("top", top);
        assertEquals(Arrays.asList("top/*"), manager.getConfigurationPaths(simple));
    }

    public void testIndexCircular() throws TypeException
    {
        try
        {
            CompositeType type = typeRegistry.register(CircularObject.class);
            manager.register("nested", type);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Cycle detected in type definition at path 'nested/nested': type 'com.zutubi.tove.config.types.CircularObject' has already appeared in this path", e.getMessage());
        }
    }

}
