package com.zutubi.validation;

import junit.framework.TestCase;
import com.zutubi.validation.sample.Jabber;
import com.zutubi.validation.providers.AnnotationValidatorProvider;
import com.zutubi.validation.bean.DefaultObjectFactory;

import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultValidationManagerTest extends TestCase
{
    private DefaultValidationManager validationManager;
    private ValidationContext validationContext;

    protected void setUp() throws Exception
    {
        super.setUp();

        validationManager = new DefaultValidationManager();
        validationContext = new DelegatingValidationContext(this);

        AnnotationValidatorProvider provider = new AnnotationValidatorProvider();
        provider.setObjectFactory(new DefaultObjectFactory());
        validationManager.addValidatorProvider(provider);
    }

    protected void tearDown() throws Exception
    {
        validationManager = null;
        validationContext = null;

        super.tearDown();
    }

    public void testValidation() throws ValidationException
    {
        Jabber j = new Jabber();
        validationManager.validate(j, validationContext);
        assertTrue(validationContext.hasErrors());
        assertTrue(validationContext.hasFieldErrors());

        List<String> fieldErrors = validationContext.getFieldErrors("host");
        assertNotNull(fieldErrors);
        assertEquals(1, fieldErrors.size());
        assertEquals("host.required", fieldErrors.get(0));
    }
}
