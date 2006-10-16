package com.zutubi.pulse.acceptance;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <class comment/>
 */
public class ServerAcceptanceTestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(StartupShutdownAcceptanceTest.class);
        suite.addTestSuite(UpgradeAcceptanceTest.class);

        return suite;
    }
}
