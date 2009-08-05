package com.zutubi.pulse.acceptance;

import java.io.File;
import java.io.IOException;

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
     * Subversion URL for an ant-ivy project that publishes to the internal pulse artifact repository. 
     */
    public static final String IVY_ANT_REPOSITORY = SUBVERSION_ACCEPT_REPO + "ivyant";
    /**
     * Subversion URL for the dep-ant project that supports creating files and asserting the existance
     * of files.  For testing, these files simulate artifacts.
     */
    public static final String DEP_ANT_REPOSITORY = SUBVERSION_ACCEPT_REPO + "depant";
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
    public static final String VERSIONED_PULSE_FILE = "pulse/pulse.xml";

    /**
     * @return the url of the test git repository
     */
    public static String getGitUrl() throws IOException
    {
        // the git repository is located on the local file system in the work.dir/git-repo directory
        File workingDir = AcceptanceTestUtils.getWorkingDirectory();
        File repositoryBase = new File(workingDir, "git-repo");
        return "file://" + repositoryBase.getCanonicalPath();
    }

    /**
     * The constants for the property names in the ProjectConfiguration class.
     */
    public static class Project
    {
        public static final String NAME = "name";
        public static final String HOOKS = "buildHooks";
        public static final String REQUIREMENTS = "requirements";
        public static final String STAGES = "stages";
        public static final String SCM = "scm";
        public static final String TYPE = "type";
        public static final String OPTIONS = "options";
        public static final String REPORT_GROUPS = "reportGroups";
        public static final String ORGANISATION = "organisation";
        
        /**
         * Property names in the multi recipe type class.
         */
        public static class MultiRecipeType
        {
            public static final String DEFAULT_RECIPE  = "default";

            public static final String RECIPES = "recipes";
            public static final String NAME = "name";
            public static class Recipe
            {
                public static final String DEFAULT_COMMAND = "build";

                public static final String COMMANDS = "commands";
            }
        }

        /**
         * Property names in the versioned type class.
         */
        public static class VersionedType
        {
            public static final String PULSE_FILE_NAME = "pulseFileName";
        }

        /**
         * The constants for the property names in the command config class.
         */
        public static class Command
        {
            public static final String NAME = "name";
            public static final String CAPTURES = "outputs";

            /**
             * The constants for the property names in the ArtifactConfiguration class.
             */
            public static class Output
            {
                public static final String NAME = "name";
                public static final String POSTPROCESSORS = "postProcessors";
            }

            /**
             * The constants for the property names in the FileOutputConfiguration class.
             */
            public static class FileOutput extends Output
            {
                public static final String FILE = "file";
            }

            /**
             * The constants for the property names in the DirectoryOutputConfiguration class.
             */
            public static class DirectoryOutput extends Output
            {
                public static final String BASE = "base";
                public static final String INCLUSIONS = "inclusions";
                public static final String EXCLUSIONS = "exclusions";
                public static final String MIME_TYPE = "type";
            }
        }

        /**
         * Property names in the ant command class.
         */
        public static class AntCommand extends Command
        {
            public static final String ARGUMENTS = "args";
            public static final String TARGETS = "targets";
        }

        /**
         * Property names in the custom fields command class.
         */
        public static class CustomFieldsCommand extends Command
        {
            public static final String FIELDS = "fields";

            public static class Field
            {
                public static final String NAME = "name";
                public static final String VALUE = "value";
                public static final String SCOPE = "scope";
            }
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
         * Build stage properties.
         */
        public static class Stage
        {
            public static final String NAME = "name";
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
            public static final String RETAIN_WORKING_COPY = "retainWorkingCopy";
        }

        public class Cleanup
        {
            public static final String NAME = "name";
            public static final String RETAIN = "retain";
            public static final String UNIT = "unit";
            public static final String WHAT = "what";
            public static final String CLEANUP_ALL = "cleanupAll";
        }

        public class Dependencies
        {
            public static final String RETRIEVAL_PATTERN = "retrievalPattern";
            public static final String STATUS = "status";
            public static final String VERSION = "version";
        }

        public static class Artifact
        {
            public static final String NAME = "name";
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

    public class Settings
    {
        public class Repository
        {
            public static final String PATH = "settings/repository";

            public static final String READ_ACCESS = "readAccess";
            public static final String WRITE_ACCESS = "writeAccess";
        }
    }
}
