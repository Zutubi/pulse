package com.zutubi.prototype.type;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.record.RecordManager;
import junit.framework.TestCase;

/**
 *
 *
 */
public abstract class TypeTestCase extends TestCase
{
    protected TypeRegistry typeRegistry;
    protected RecordManager recordManager;
    protected ConfigurationTemplateManager configurationTemplateManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeRegistry = new TypeRegistry();
        recordManager = new RecordManager();
        configurationTemplateManager = new ConfigurationTemplateManager();
        typeRegistry.setConfigurationTemplateManager(configurationTemplateManager);
        typeRegistry.setHandleAllocator(recordManager);
    }

    protected void tearDown() throws Exception
    {
        typeRegistry = null;
        configurationTemplateManager = null;
        super.tearDown();
    }
}
