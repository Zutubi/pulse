package com.zutubi.prototype.type;

import junit.framework.TestCase;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 *
 *
 */
public class TypeTestCase extends TestCase
{
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

    public void test()
    {
        // 
    }
}
