package com.zutubi.pulse.acceptance;

import junit.extensions.TestSetup;
import junit.framework.Test;

import java.util.Properties;

/**
 * <class-comment/>
 */
public class PreserveSystemPropertiesSetup extends TestSetup
{
    private Properties sys;
    public PreserveSystemPropertiesSetup(Test test)
    {
        super(test);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        sys = new Properties();
        sys.putAll(System.getProperties());
    }

    protected void tearDown() throws Exception
    {
        System.getProperties().clear();
        System.getProperties().putAll(sys);

        super.tearDown();
    }
}

