package com.zutubi.tove.type;

import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.record.RecordManager;
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
        configurationTemplateManager.setTypeRegistry(typeRegistry);
        typeRegistry.setHandleAllocator(recordManager);
    }

    protected void tearDown() throws Exception
    {
        typeRegistry = null;
        configurationTemplateManager = null;
        super.tearDown();
    }
}
