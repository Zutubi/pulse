package com.zutubi.i18n.error;

import com.zutubi.util.junit.ZutubiTestCase;

/**
 * <class-comment/>
 */
public class ErrorHandlerTest extends ZutubiTestCase
{
    private ErrorHandler handler;

    protected void setUp() throws Exception
    {
        super.setUp();

        handler = new ErrorHandler();
    }

    protected void tearDown() throws Exception
    {
        handler = null;

        super.tearDown();
    }

    public void test404Error()
    {
        handler.setBundle("com/zutubi/i18n/mock/errors");

        assertEquals("404: Page not found.", handler.error(ErrorCode.error(404)));

    }

}
