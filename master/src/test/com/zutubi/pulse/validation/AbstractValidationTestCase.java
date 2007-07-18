package com.zutubi.pulse.validation;

import com.zutubi.prototype.config.AbstractConfigurationSystemTestCase;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.pulse.core.config.Configuration;

import java.util.List;

/**
 * A base for validation test cases, allowing conventient testing of the
 * validation rules for configuration types.
 */
public class AbstractValidationTestCase extends AbstractConfigurationSystemTestCase
{
    private ConfigurationRegistry configurationRegistry;

    protected void setUp() throws Exception
    {
        super.setUp();
        configurationRegistry = new ConfigurationRegistry();
        configurationRegistry.setConfigurationPersistenceManager(configurationPersistenceManager);
        configurationRegistry.setConfigurationTemplateManager(configurationTemplateManager);
        configurationRegistry.setTypeRegistry(typeRegistry);
        configurationRegistry.initSetup();
        configurationRegistry.init();
    }

    protected void tearDown() throws Exception
    {
        configurationRegistry = null;
        super.tearDown();
    }

    protected void assertValid(String parentPath, String baseName, Configuration instance) throws TypeException
    {
        instance = configurationTemplateManager.validate(parentPath, baseName, unstantiate(instance));
        assertTrue(instance.isValid());
    }

    protected void assertFieldErrors(String parentPath, String baseName, Configuration instance, String field, String... expectedErrors) throws TypeException
    {
        instance = configurationTemplateManager.validate(parentPath, baseName, unstantiate(instance));
        List<String> fieldErrors = instance.getFieldErrors(field);
        assertEquals(expectedErrors.length, fieldErrors.size());
        for(int i = 0; i < expectedErrors.length; i++)
        {
            assertEquals(expectedErrors[i], fieldErrors.get(i));
        }
    }
}
