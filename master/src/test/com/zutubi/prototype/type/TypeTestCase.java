package com.zutubi.prototype.type;

import junit.framework.TestCase;

/**
 *
 *
 */
public class TypeTestCase extends TestCase
{
    protected TypeRegistry typeRegistry;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeRegistry = new TypeRegistry();
    }

    protected void tearDown() throws Exception
    {
        typeRegistry = null;
        super.tearDown();
    }
}
