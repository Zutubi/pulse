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
     * Subversion URL for a versioned pulse file project.
     */
    public static final String VERSIONED_REPOSITORY = SUBVERSION_ACCEPT_REPO + "testversioned";
    /**
     * Subversion URL for a small sample ant project that has an OCUnit output file results.txt.
     */
    public static final String OCUNIT_REPOSITORY = SUBVERSION_ACCEPT_REPO + "ocunit";

    /**
     * The constants for the property names in the ProjectConfiguration class.
     */
    public static class Project
    {
        public static final String HOOKS = "buildHooks";
        public static final String STAGES = "stages";
        public static final String SCM = "scm";
        public static final String TYPE = "type";
        public static final String LABELS = "labels";
        public static final String OPTIONS = "options";
        public static final String REPORT_GROUPS = "reportGroups";

        /**
         * Property names shared by template types.
         */
        public static class TemplateType
        {
            public static final String ARTIFACTS = "artifacts";
        }

        /**
         * Property names in the ant type class.
         */
        public static class AntType extends TemplateType
        {
            public static final String ARGUMENTS = "args";
            public static final String TARGETS = "target";
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
            public static final String CHECKOUT_SCHEME = "checkoutScheme";
            public static final String QUIET_PERIOD_ENABLED = "quietPeriodEnabled";
        }

        /**
         * The constants for the property names in the project template type class.
         */
        public static class Type
        {
            public static final String ARTIFACTS = "artifacts";
        }

        /**
         * Build stage properties.
         */
        public static class Stage
        {
            public static final String RECIPE = "recipe";
            public static final String AGENT = "agent";
        }

        /**
         * Generic build options.
         */
        public class Options
        {
            public static final String AUTO_CLEAR_RESPONSIBILITY = "autoClearResponsibility";
            public static final String ID_LEADER = "idLeader";
            public static final String PERSISTENT_WORK_DIR = "persistentWorkDir";
        }
    }

    /**
     * The constants for the property names in the ArtifactConfiguration class.
     */
    public static class Artifact
    {
        public static final String NAME = "name";
        public static final String POSTPROCESSORS = "postprocessors";
    }

    /**
     * Property names in the FileArtifactConfiguration class.
     */
    public static class FileArtifact extends Artifact
    {
        public static final String FILE = "file";
    }

    /**
     * The constants for the property names in the DirectoryArtifactConfiguration class.
     */
    public static class DirectoryArtifact extends Artifact
    {
        public static final String BASE = "base";
        public static final String INCLUDES = "includes";
        public static final String EXCLUDES = "excludes";
        public static final String MIME_TYPE = "type";
    }
}
