package com.zutubi.pulse.master.validation;

import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.config.AbstractConfigurationSystemTestCase;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.tove.type.TypeException;

import java.util.List;

/**
 * A base for validation test cases, allowing conventient testing of the
 * validation rules for configuration types.
 */
public abstract class AbstractValidationTestCase extends AbstractConfigurationSystemTestCase
{
    private ConfigurationRegistry configurationRegistry;

    protected void setUp() throws Exception
    {
        super.setUp();
        configurationRegistry = new ConfigurationRegistry();
        configurationRegistry.setConfigurationPersistenceManager(configurationPersistenceManager);
        configurationRegistry.setConfigurationSecurityManager(configurationSecurityManager);
        configurationRegistry.setTypeRegistry(typeRegistry);
        configurationRegistry.setActionManager(new ActionManager());
        configurationRegistry.initSetup();
        configurationRegistry.init();
    }

    protected void tearDown() throws Exception
    {
        configurationRegistry = null;
        super.tearDown();
    }

    protected void validatedAndAssertValid(String parentPath, String baseName, Configuration instance) throws TypeException
    {
        instance = doValidation(parentPath, baseName, instance);
        assertTrue(instance.isValid());
    }

    protected void validateAndAssertInstanceErrors(String parentPath, String baseName, Configuration instance, String... expectedErrors) throws TypeException
    {
        instance = doValidation(parentPath, baseName, instance);
        assertInstanceErrors(instance, expectedErrors);
    }


    protected void validateAndAssertFieldErrors(String parentPath, String baseName, Configuration instance, String field, String... expectedErrors) throws TypeException
    {
        instance = doValidation(parentPath, baseName, instance);
        assertFieldErrors(instance, field, expectedErrors);
    }

    protected void assertErrors(List<String> gotErrors, String... expectedErrors)
    {
        assertEquals(expectedErrors.length, gotErrors.size());
        for(int i = 0; i < expectedErrors.length; i++)
        {
            assertEquals(expectedErrors[i], gotErrors.get(i));
        }
    }

    protected void assertInstanceErrors(Configuration instance, String... expectedErrors)
    {
        assertErrors(instance.getInstanceErrors(), expectedErrors);
    }

    protected void assertFieldErrors(Configuration instance, String field, String... expectedErrors)
    {
        assertErrors(instance.getFieldErrors(field), expectedErrors);
    }

    protected Configuration doValidation(String parentPath, String baseName, Configuration instance) throws TypeException
    {
        instance = configurationTemplateManager.validate(parentPath, baseName, unstantiate(instance), true, false);
        return instance;
    }
}
