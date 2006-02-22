package com.cinnamonbob.acceptance;

import net.sourceforge.jwebunit.WebTestCase;

/**
 *
 */
public abstract class ExtendedWebTestCase extends WebTestCase
{
    public ExtendedWebTestCase()
    {
    }

    public ExtendedWebTestCase(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        getTestContext().setBaseUrl("http://localhost:8080/");
    }
}
