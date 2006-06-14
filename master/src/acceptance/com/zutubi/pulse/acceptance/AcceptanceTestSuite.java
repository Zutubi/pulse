package com.zutubi.pulse.acceptance;

import junit.framework.Test;
import junit.framework.TestSuite;

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
        suite.addTestSuite(AdminXmlRpcAcceptanceTest.class);
        suite.addTestSuite(AnonymousAccessAcceptanceTest.class);
        suite.addTestSuite(GeneralConfigurationAcceptanceTest.class);
        suite.addTestSuite(LicenseManagementAcceptanceTest.class);
        suite.addTestSuite(ProjectAcceptanceTest.class);
        suite.addTestSuite(ProjectArtifactsAcceptanceTest.class);
        suite.addTestSuite(ProjectWizardAcceptanceTest.class);
        suite.addTestSuite(RssAcceptanceTest.class);
        suite.addTestSuite(SmtpConfigurationAcceptanceTest.class);
        suite.addTestSuite(ResourceAcceptanceTest.class);
        suite.addTestSuite(SlaveAcceptanceTest.class);
        suite.addTestSuite(JabberConfigurationAcceptanceTest.class);
        suite.addTestSuite(UserAdministrationAcceptanceTest.class);
        suite.addTestSuite(UserPreferencesAcceptanceTest.class);
        suite.addTestSuite(UserSecurityAcceptanceTest.class);
        suite.addTestSuite(WebAppAcceptanceTest.class);
        return suite;
    }
}
