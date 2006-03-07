package com.cinnamonbob.acceptance;

import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * <class-comment/>
 */
public class AcceptanceTestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        // need to run this one first as it runs through the installation setup.
        suite.addTestSuite(SetupAcceptanceTest.class);

        // now we can run the rest of the tests.
        suite.addTestSuite(GeneralConfigurationAcceptanceTest.class);
        suite.addTestSuite(SmtpConfigurationAcceptanceTest.class);
        suite.addTestSuite(UserAdministrationAcceptanceTest.class);
        suite.addTestSuite(UserPreferencesAcceptanceTest.class);
        return suite;
    }
}
