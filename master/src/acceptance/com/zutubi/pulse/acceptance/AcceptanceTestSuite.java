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

        //---( xml rpc acceptance tests )---
        suite.addTestSuite(AdminXmlRpcAcceptanceTest.class);
        suite.addTestSuite(ProjectXmlRpcAcceptanceTest.class);

        //---( web ui acceptance tests )---
        suite.addTestSuite(LicenseAuthorisationAcceptanceTest.class);
        suite.addTestSuite(LicenseManagementAcceptanceTest.class);
        suite.addTestSuite(ProjectGroupAcceptanceTest.class);
        suite.addTestSuite(ProjectAcceptanceTest.class);
        // AnonymousAccessAcceptanceTest needs some projects, so order it here for now.
        suite.addTestSuite(AnonymousAccessAcceptanceTest.class);
        // AnonymousAccessAcceptanceTest tests the default state of that setting, so
        // run this test (that messes with the setting) afterwards.
        suite.addTestSuite(GeneralConfigurationAcceptanceTest.class);
        suite.addTestSuite(ProjectArtifactsAcceptanceTest.class);
        suite.addTestSuite(ProjectWizardAcceptanceTest.class);
        suite.addTestSuite(RssAcceptanceTest.class);
        suite.addTestSuite(SmtpConfigurationAcceptanceTest.class);
        suite.addTestSuite(ResourceAcceptanceTest.class);
        suite.addTestSuite(SlaveAcceptanceTest.class);
        suite.addTestSuite(JabberConfigurationAcceptanceTest.class);
        suite.addTestSuite(LdapConfigurationAcceptanceTest.class);
        suite.addTestSuite(CommitMessageTransformerAcceptanceTest.class);
        suite.addTestSuite(ChangeViewersAcceptanceTest.class);
//        suite.addTestSuite(StartupShutdownAcceptanceTest.class);
        suite.addTestSuite(UserAdministrationAcceptanceTest.class);
        suite.addTestSuite(UserPreferencesAcceptanceTest.class);
        suite.addTestSuite(UserSecurityAcceptanceTest.class);
        suite.addTestSuite(GroupAcceptanceTest.class);
        suite.addTestSuite(WebAppAcceptanceTest.class);
        return suite;
    }
}
