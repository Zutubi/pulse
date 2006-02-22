package com.cinnamonbob.acceptance;

import net.sourceforge.jwebunit.WebTestCase;

/**
 *
 */
public class ExtendedWebTestCase extends WebTestCase
{
    public ExtendedWebTestCase()
    {
    }

    public ExtendedWebTestCase(String name)
    {
        super(name);
    }

    public void test()
    {
        // these acceptance tests should not be executed as part of the regular test cases.
    }
}
