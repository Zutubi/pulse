package com.zutubi.i18n.error;

import junit.framework.TestCase;
import com.zutubi.i18n.mock.MockErrors;
import com.zutubi.i18n.mock.MockInvalidErrors;

/**
 * <class-comment/>
 */
public class ErrorsTest extends TestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testValidErrorCodes()
    {
        assertTrue(Errors.validateErrorCodes(MockErrors.class));
    }

    public void testInvalidErrorCodes()
    {
        assertFalse(Errors.validateErrorCodes(MockInvalidErrors.class));
    }
}
