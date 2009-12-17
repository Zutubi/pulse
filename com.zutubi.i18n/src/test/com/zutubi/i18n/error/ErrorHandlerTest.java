package com.zutubi.i18n.error;

import com.zutubi.util.junit.ZutubiTestCase;

public class ErrorHandlerTest extends ZutubiTestCase
{
    private ErrorHandler handler;

    protected void setUp() throws Exception
    {
        super.setUp();

        handler = new ErrorHandler();
    }

    public void test404Error()
    {
        handler.setBundle("com/zutubi/i18n/types/errors");
        assertEquals("404: Page not found.", handler.error(ErrorCode.error(404)));
    }

}
