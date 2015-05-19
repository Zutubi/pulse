package com.zutubi.pulse.core.engine.api;

/**
 * Defines the property names for various build properties.
 */
public class BuildProperties
{
    //-----------------------------------------------------------------------
    // Namespaces
    //-----------------------------------------------------------------------

    /**
     * Namespace for internal properties used by Pulse itself, used to keep
     * such properties safe from accidental overriding.
     */
    public static final String NAMESPACE_INTERNAL = "internal";
    /**
     * Namespace for user-configured properties.
     */
    public static final String NAMESPACE_USER     = "user";

    //-----------------------------------------------------------------------
    // Scope labels
    //-----------------------------------------------------------------------

    /**
     * Labels the outermost scope for a recipe.
     */
    public static final String SCOPE_RECIPE = "recipe";
    
    //-----------------------------------------------------------------------
    // Shared properties
    //-----------------------------------------------------------------------

    /**
     * The status of a build, stage or command result as a readable, lower-case
     * string.
     */
    public static final String PROPERTY_STATUS                  = "status";

    //-----------------------------------------------------------------------
    // Build level properties
    //-----------------------------------------------------------------------

    /**
     * The number of times the project has been built before the running
     * build.
     */
    public static final String PROPERTY_BUILD_COUNT             = "build.count";
    /**
     * The absolute path of the directory where build artifacts will be
     * stored on the master.
     */
    public static final String PROPERTY_BUILD_DIRECTORY         = "build.dir";
    /**
     * The build result's id, unique across all builds.
     */
    public static final String PROPERTY_BUILD_ID                = "build.id";
    /**
     * The build's identifying number, unique (and increasing) within a project.
     */
    public static final String PROPERTY_BUILD_NUMBER            = "build.number";
    /**
     * A short string describing the reason that the build was triggered.
     */
    public static final String PROPERTY_BUILD_REASON            = "build.reason";
    /**
     * The SCM revision being built, in string form.
     */
    public static final String PROPERTY_BUILD_REVISION          = "build.revision";
    /**
     * The time that the build commenced, in the format yyyy-MM-dd HH:mm.
     */
    public static final String PROPERTY_BUILD_TIMESTAMP         = "build.timestamp";
    /**
     * The time that the build commenced, as the number of milliseconds since
     * midnight, January 1, 1970 UTC.
     */
    public static final String PROPERTY_BUILD_TIMESTAMP_MILLIS  = "build.timestamp.millis";
    /**
     * If the build was started by a configured trigger, the name of that trigger.
     */
    public static final String PROPERTY_BUILD_TRIGGER           = "build.trigger";
    /**
     * The resolved version string for this build, used to indentify this build.  It
     * differs from the build number in that the build number is a programatic identifier,
     * where as the build version is a human readable identifier.
     */
    public static final String PROPERTY_BUILD_VERSION           = "build.version";
    /**
     * If non-empty this specifies a subdirectory of the base that should be used for the source
     * code checkout.  (Note the checkout itself may happen in another directory and by copied
     * here).
     */
    public static final String PROPERTY_CHECKOUT_SUBDIR         = "checkout.subdir";
    /**
     * An internal property used by the master to instruct agents to compress
     * captured artifacts for download.
     */
    public static final String PROPERTY_COMPRESS_ARTIFACTS      = "compress.artifacts";
    /**
     * The dependency descriptor instance for a build.  This is an instance of the ivy ModuleDescriptor.
     */
    public static final String PROPERTY_DEPENDENCY_DESCRIPTOR   = "dependency.descriptor";
    /**
     * If true, send command output back to the master as the build runs for
     * live stage logs.
     */
    public static final String PROPERTY_ENABLE_LIVE_LOGS        = "enable.live.logs";
    /**
     * Set to "true" if the SCM bootstrap for the build is incremental, "false"
     * otherwise.  Different to incremental.build: incremental bootstraps are
     * also used for the "clean update" scheme, for example.
     */
    public static final String PROPERTY_INCREMENTAL_BOOTSTRAP   = "incremental.bootstrap";
    /**
     * Set to "true" if the build uses the incremental updated checkout
     * scheme, "false" otherwise.
     */
    public static final String PROPERTY_INCREMENTAL_BUILD       = "incremental.build";
    /**
     * Set to "true" for a local build, "false" otherwise.
     */
    public static final String PROPERTY_LOCAL_BUILD             = "local.build";
    /**
     * The URL of the Pulse master (as configured in the master settings).
     */
    public static final String PROPERTY_MASTER_URL              = "master.url";
    /**
     * The project name for project builds, the user login for personal builds.
     */
    public static final String PROPERTY_OWNER                   = "owner";
    /**
     * Specifies the location of the persistent working directory for a project
     * on an agent.  Supports limited properties (as it is resolved when the
     * full context is not available).
     */
    public static final String PROPERTY_PERSISTENT_WORK_PATTERN = "persistent.work.pattern";
    /**
     * Specifies the location of the temporary working directory for a project
     * on an agent.  Supports limited properties (as it is resolved when the
     * full context is not available).
     */
    public static final String PROPERTY_TEMP_PATTERN            = "temp.pattern";
    /**
     * Set to "true" for a personal build, "false" otherwise.
     */
    public static final String PROPERTY_PERSONAL_BUILD          = "personal.build";
    /**
     * For personal builds, the login of the user running the build.
     */
    public static final String PROPERTY_PERSONAL_USER           = "personal.user";
    /**
     * The name of the organisation of the project being built.
     */
    public static final String PROPERTY_ORGANISATION            = "organisation";
    /**
     * The name of the project being built.
     */
    public static final String PROPERTY_PROJECT                 = "project";
    /**
     * The unique 64-bit handle of the project being built.
     */
    public static final String PROPERTY_PROJECT_HANDLE          = "project.handle";
    /**
     * The version of Pulse running the build.
     */
    public static final String PROPERTY_PULSE_VERSION           = "pulse.version";
    /**
     * The pattern that defines where retrieved dependencies are to be delivered.
     */
    public static final String PROPERTY_RETRIEVAL_PATTERN       = "retrieval.pattern";
    /**
     * The SCM configuration for the project being built.
     */
    public static final String PROPERTY_SCM_CONFIGURATION       = "scm.config";
    /**
     * The number of times the project has been built successfully before the
     * running build.
     */
    public static final String PROPERTY_SUCCESS_COUNT           = "success.count";
    /**
     * Controls whether dependency retrieval synchronises its destination
     * directory (i.e. removes any files it does not put there).
     */
    public static final String PROPERTY_SYNC_DESTINATION       = "sync.destination";
    public static final boolean DEFAULT_SYNC_DESTINATION       = true;
    /**
     * A short, human-readable summary of the test results for the build
     * (only available post-build).
     */
    public static final String PROPERTY_TEST_SUMMARY            = "test.summary";
    /**
     * Indicates if zip files retrieved from the artifact repository should be automatically
     * unzipped.
     */
    public static final String PROPERTY_UNZIP_RETRIEVED_ARCHIVES = "unzip.retrieved.archives";

    //-----------------------------------------------------------------------
    // Stage/Recipe level properties
    //-----------------------------------------------------------------------

    /**
     * The name of the agent the stage is assigned to.
     */
    public static final String PROPERTY_AGENT                   = "agent";
    /**
     * The path pattern describing where an agent's data should be put.
     */
    public static final String PROPERTY_AGENT_DATA_PATTERN      = "agent.data.pattern";
    /**
     * The unique 64-bit handle of the agent the stage is assigned to.
     */
    public static final String PROPERTY_AGENT_HANDLE            = "agent.handle";
    /**
     * The absolute path of the base directory for the working copy being
     * built.
     */
    public static final String PROPERTY_BASE_DIR                = "base.dir";
    /**
     * Internal property used to collect custom field values for a recipe.
     */
    public static final String PROPERTY_CUSTOM_FIELDS           = "custom.fields";
    /**
     * The absolute path of the data directory for the agent running the
     * recipe.
     */
    public static final String PROPERTY_DATA_DIR                = "data.dir";
    /**
     * An internal property used by agents to locate files needed during the
     * build.
     */
    public static final String PROPERTY_FILE_REPOSITORY         = "file.repository";
    /**
     * The unique 64-bit id of the host the stage is assigned to.
     */
    public static final String PROPERTY_HOST_ID                 = "host.id";
    /**
     * Comma-separated names of directories to ignore when copying source.
     */
    public static final String PROPERTY_IGNORE_DIRS            = "ignore.dirs";
    /**
     * An internal property used to create patch formats.
     */
    public static final String PROPERTY_PATCH_FORMAT_FACTORY    = "patch.format.factory";
    /**
     * The name of the recipe being built, may be "[default]" if no recipe
     * was explicitly specified.
     */
    public static final String PROPERTY_RECIPE                  = "recipe";
    /**
     * Unique identifier for the recipe being built.
     */
    public static final String PROPERTY_RECIPE_ID               = "recipe.id";
    /**
     * Internal property used by the agent to determine which directories to
     * use for build processing.
     */
    public static final String PROPERTY_RECIPE_PATHS            = "recipe.paths";
    /**
     * The current status of a running recipe, added to the context of commands
     * during recipe processing.
     */
    public static final String PROPERTY_RECIPE_STATUS           = "recipe.status";
    /**
     * The time that the recipe commenced, in the format yyyy-MM-dd HH:mm.
     */
    public static final String PROPERTY_RECIPE_TIMESTAMP        = "recipe.timestamp";
    /**
     * The time that the recipe commenced, as the number of milliseconds
     * since midnight, January 1, 1970 UTC.
     */
    public static final String PROPERTY_RECIPE_TIMESTAMP_MILLIS = "recipe.timestamp.millis";
    /**
     * Internal property used by agents to locate resources imported into the
     * build.
     */
    public static final String PROPERTY_RESOURCE_REPOSITORY     = "resource.repository";
    /**
     * Internal property used to create SCM clients.
     */
    public static final String PROPERTY_SCM_CLIENT_FACTORY      = "scm.client.factory";
    /**
     * Internal property used to indicate the checkout step should be skipped.
     */
    public static final String PROPERTY_SKIP_CHECKOUT           = "skip.checkout";
    /**
     * The name of the stage being built.
     */
    public static final String PROPERTY_STAGE                   = "stage";
    /**
     * The handle of the stage being built.
     */
    public static final String PROPERTY_STAGE_HANDLE            = "stage.handle";
    /**
     * Internal property used to collect test results for a recipe.
     */
    public static final String PROPERTY_TEST_RESULTS            = "test.results";

    //-----------------------------------------------------------------------
    // Command level properties
    //-----------------------------------------------------------------------

    /**
     * Absolute path of the output directory for a command (where artifacts
     * are captured to).
     */
    public static final String PROPERTY_OUTPUT_DIR             = "output.dir";

    //-----------------------------------------------------------------------
    // Other constants
    //-----------------------------------------------------------------------

    /**
     * Suffix used to identify a property as referring to a directory.
     */
    public static final String SUFFIX_DIRECTORY = "dir";
    /**
     * Standard date format for timestamp properties.
     */
    public static final String TIMESTAMP_FORMAT_STRING = "yyyy-MM-dd HH:mm";
}
