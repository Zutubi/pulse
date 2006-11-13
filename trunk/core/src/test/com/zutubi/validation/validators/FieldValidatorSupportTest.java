package com.zutubi.validation.validators;

import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.i18n.InMemoryTextProvider;
import junit.framework.TestCase;

/**
 * <class-comment/>
 */
public class FieldValidatorSupportTest extends TestCase
{
    private FieldValidatorSupport fieldSupport;
    private InMemoryTextProvider textProvider;

    protected void setUp() throws Exception
    {
        super.setUp();

        textProvider = new InMemoryTextProvider();

        fieldSupport = new NoopFieldValidator();
        fieldSupport.setFieldName("field");
        fieldSupport.setValidationContext(new DelegatingValidationContext(textProvider));
    }

    protected void tearDown() throws Exception
    {
        fieldSupport = null;
        textProvider = null;

        super.tearDown();
    }

    public void testNoConfigurationMessage()
    {
        assertEquals("no.message.available", fieldSupport.getMessage());
    }

    public void testUnavailableMessageKeyMessage()
    {
        fieldSupport.setMessageKey("unavailable.message.key");
        assertEquals("unavailable.message.key", fieldSupport.getMessage());
    }

    public void testDefaultMessage()
    {
        fieldSupport.setMessageKey("unavailable.message.key");
        fieldSupport.setDefaultMessage("Default Message");
        assertEquals("Default Message", fieldSupport.getMessage());
    }

    public void testAvailableMessageKeyMessage()
    {
        textProvider.addText("available.message.key", "Available Message");

        fieldSupport.setMessageKey("available.message.key");
        fieldSupport.setDefaultMessage("Default Message");
        assertEquals("Available Message", fieldSupport.getMessage());
    }
}
