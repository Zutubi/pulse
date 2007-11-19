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
        suite.addTestSuite(ConfigXmlRpcAcceptanceTest.class);
        suite.addTestSuite(ProjectXmlRpcAcceptanceTest.class);
        suite.addTestSuite(ResourceAcceptanceTest.class);

        //---( web ui acceptance tests )---
        suite.addTestSuite(ConfigUIAcceptanceTest.class);
        suite.addTestSuite(DeleteAcceptanceTest.class);
        suite.addTestSuite(PluginUIAcceptanceTest.class);
        suite.addTestSuite(GroupAcceptanceTest.class);
        suite.addTestSuite(ProjectLabelAcceptanceTest.class);
        suite.addTestSuite(AnonymousAccessAcceptanceTest.class);
        suite.addTestSuite(ServerSectionAcceptanceTest.class);

        //---( build tests )---
        suite.addTestSuite(BuildAcceptanceTest.class);
        suite.addTestSuite(BuildHookAcceptanceTest.class);
        
        //---( other acceptance tests )---
//        suite.addTestSuite(StartupShutdownAcceptanceTest.class);
        return suite;
    }
}
