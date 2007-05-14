package com.zutubi.prototype.type;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.record.RecordManager;
import junit.framework.TestCase;

/**
 *
 *
 */
public class TypeTestCase extends TestCase
{
    protected RecordManager recordManager;
    protected ConfigurationPersistenceManager configurationPersistenceManager;
    protected TypeRegistry typeRegistry;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeRegistry = new TypeRegistry();
        configurationPersistenceManager = new ConfigurationPersistenceManager();
        configurationPersistenceManager.setTypeRegistry(typeRegistry);
        configurationPersistenceManager.init();
    }

    protected void tearDown() throws Exception
    {
        typeRegistry = null;
        configurationPersistenceManager = null;
        super.tearDown();
    }
}
