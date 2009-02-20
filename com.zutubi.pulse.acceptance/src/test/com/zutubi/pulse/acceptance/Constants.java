package com.zutubi.pulse.acceptance;

/**
 * General bucket for constants used in acceptance tests.
 */
public class Constants
{
    /**
     * Subversion base URL for several test projects (see below).
     */
    private static final String SUBVERSION_ACCEPT_REPO = "svn://localhost:3088/accept/trunk/";
    /**
     * Subversion URL for a trivial ant project (just a build.xml file).
     */
    public static final String TRIVIAL_ANT_REPOSITORY = SUBVERSION_ACCEPT_REPO + "triviant";
    /**
     * Subversion URL for a small sample ant project that contains a build file as well as src and
     * test directories.
     */
    public static final String TEST_ANT_REPOSITORY = SUBVERSION_ACCEPT_REPO + "testant";
    /**
     * Subversion URL for a small sample maven project that contains the pom.xml, along with a
     * source and test file.
     */
    public static final String TEST_MAVEN_REPOSITORY = SUBVERSION_ACCEPT_REPO + "testmaven";
    /**
     * Subversion URL for an ant project which will wait up to 5 minutes for a file to
     * appear.
     */
    public static final String WAIT_ANT_REPOSITORY = SUBVERSION_ACCEPT_REPO + "waitant";
    /**
     * Subversion URL for an ant project that will fail with an unknown default target exception.
     * The ant file has no targets.
     */
    public static final String FAIL_ANT_REPOSITORY = SUBVERSION_ACCEPT_REPO + "failant";

    /**
     * The constants for the property names in the ProjectConfiguration class.
     */
    public static class Project
    {
        public static final String HOOKS = "buildHooks";
        public static final String REQUIREMENTS = "requirements";
        public static final String STAGES = "stages";
        public static final String SCM = "scm";
        public static final String TYPE = "type";

        /**
         * Property names in the ant type class.
         */
        public static class AntType
        {
            public static final String ARGUMENTS = "args";
        }

        /**
         * Property names in the versioned type class.
         */
        public static class VersionedType
        {
            public static final String PULSE_FILE_NAME = "pulseFileName";
        }

        /**
         * Properties shared by all SCMs.
         */
        public static class Scm
        {
            public static final String QUIET_PERIOD_ENABLED = "quietPeriodEnabled";
        }

        /**
         * The constants for the property names in the project template type class.
         */
        public static class Command
        {
            public static final String CAPTURES = "outputs";
        }

        /**
         * Build stage properties.
         */
        public static class Stage
        {
            public static final String RECIPE = "recipe";
        }
    }

    /**
     * The constants for the property names in the ArtifactConfiguration class.
     */
    public static class Output
    {
        public static final String NAME = "name";
        public static final String POSTPROCESSORS = "postProcessors";
    }

    /**
     * The constants for the property names in the DirectoryArtifactConfiguration class.
     */
    public static class DirectoryOutput extends Output
    {
        public static final String BASE = "base";
        public static final String INCLUSIONS = "inclusions";
        public static final String EXCLUSIONS = "exclusions";
        public static final String MIME_TYPE = "type";
    }
}
