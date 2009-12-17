package com.zutubi.i18n.error;

import com.zutubi.i18n.types.TestErrors;
import com.zutubi.i18n.types.TestInvalidErrors;
import com.zutubi.util.junit.ZutubiTestCase;

/**
 * <class-comment/>
 */
public class ErrorsTest extends ZutubiTestCase
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
        assertTrue(Errors.validateErrorCodes(TestErrors.class));
    }

    public void testInvalidErrorCodes()
    {
        assertFalse(Errors.validateErrorCodes(TestInvalidErrors.class));
    }
}
