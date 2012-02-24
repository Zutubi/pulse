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
    public static final String SUBVERSION_ACCEPT_REPO = "svn://localhost:3088/accept/trunk/";
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
     * Subversion URL for a small maven project that contains two pom files.  The pom-artifact1.xml
     * is configured to produce the zutubi/artifact1-1.0.jar file and contains reference to the
     * pulse artifact repository for deployment.  The pom-artifact2.xml is configured to retrieve
     * the artifact1.jar as a dependency.
     */
    public static final String DEP_MAVEN_REPOSITORY = SUBVERSION_ACCEPT_REPO + "depmaven";
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
     * Subversion URL for a small sample ant project that has an OCUnit output file results.txt.
     */
    public static final String OCUNIT_REPOSITORY = SUBVERSION_ACCEPT_REPO + "ocunit";
    /**
     * Subversion URL for a small sample ant project that generates information for testing the
     * details tab.
     */
    public static final String ALL_ANT_REPOSITORY = SUBVERSION_ACCEPT_REPO + "allant";

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
     * @return the location of the test mercurial repository
     */
    public static String getMercurialRepository() throws IOException
    {
        // located on the local file system in the work.dir/hg-repo directory
        File workingDir = AcceptanceTestUtils.getWorkingDirectory();
        return new File(workingDir, "hg-repo").getCanonicalPath();
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
        public static final String BOOTSTRAP = "bootstrap";
        public static final String SCM = "scm";
        public static final String TYPE = "type";
        public static final String LABELS = "labels";
        public static final String OPTIONS = "options";
        public static final String REPORT_GROUPS = "reportGroups";
        public static final String ORGANISATION = "organisation";
        public static final String PERMISSIONS = "permissions";
        public static final String POST_PROCESSORS = "postProcessors";
        public static final String CONTACTS = "contacts";
        public static final String DEPENDENCIES = "dependencies";
        public static final String COMMIT_MESSAGE_TRANSFORMERS = "commitMessageTransformers";

        /**
         * Property names in the multi recipe type class.
         */
        public static class MultiRecipeType
        {
            public static final String DEFAULT_RECIPE = "defaultRecipe";
            public static final String DEFAULT_RECIPE_NAME = "default";
            public static final String RECIPES = "recipes";

            public static class Recipe
            {
                public static final String NAME = "name";
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
            public static final String ARTIFACTS = "artifacts";

            /**
             * The constants for the property names in the ArtifactConfiguration class.
             */
            public static class Artifact
            {
                public static final String NAME = "name";
                public static final String POSTPROCESSORS = "postProcessors";
                public static final String PUBLISH = "publish";
                public static final String FEATURED = "featured";
            }
            
            /**
             * The constants for the property names in the FileArtifactConfiguration class.
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
         * The constants for the property names in the BootstrapConfiguration class.
         */
        public static class Bootstrap
        {
            public static final String CHECKOUT_TYPE = "checkoutType";
            public static final String BUILD_TYPE = "buildType";
            public static final String PERSISTENT_DIR_PATTERN = "persistentDirPattern";
            public static final String TEMP_DIR_PATTERN = "tempDirPattern";
        }

        /**
         * Properties shared by all SCMs.
         */
        public static class Scm
        {
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
