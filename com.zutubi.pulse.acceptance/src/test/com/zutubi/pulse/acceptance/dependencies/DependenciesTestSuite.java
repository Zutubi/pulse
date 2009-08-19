package com.zutubi.pulse.acceptance.dependencies;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This test suite includes all of the acceptance tests related to the
 * dependency feature set originally introduced in 2.1.
 *
 * These tests have been given their own suite as a way to
 * a) run them by themselves in development
 * b) help categorise the growing number of tests.
 */
public class DependenciesTestSuite
{
    public static Test suite()
    {
        TestSuite dependencySuite = new TestSuite();
        dependencySuite.addTestSuite(DependenciesAcceptanceTest.class);
        dependencySuite.addTestSuite(DependenciesConfigurationAcceptanceTest.class);
        dependencySuite.addTestSuite(DependenciesUIAcceptanceTest.class);
        dependencySuite.addTestSuite(RebuildDependenciesAcceptanceTest.class);
        dependencySuite.addTestSuite(BuildVersionAcceptanceTest.class);
        dependencySuite.addTestSuite(RepositoryPermissionsAcceptanceTest.class);

        // cleanup the artifact repository before continueing on.
        return new CleanArtifactRepository(dependencySuite);
    }

    private static class CleanArtifactRepository extends TestSetup
    {
        private Repository repository;

        private CleanArtifactRepository(Test test)
        {
            super(test);
        }

        protected void tearDown() throws Exception
        {
            repository = new Repository();
            repository.clear();

            super.tearDown();
        }
    }
}
