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
     * The build's unique identifying number.
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
     * Set to "true" if the build has been explicitly marked as a clean
     * build, "false" otherwise. Any incremental build artifacts will be
     * deleted before such a build is commenced.
     */
    public static final String PROPERTY_CLEAN_BUILD             = "clean.build";
    /**
     * An internal property used by the master to instruct agents to compress
     * captured artifacts for download.
     */
    public static final String PROPERTY_COMPRESS_ARTIFACTS      = "compress.artifacts";
    /**
     * An internal property used by the master to instruct agents to compress
     * a working copy snapshot for download.
     */
    public static final String PROPERTY_COMPRESS_WORKING_DIR    = "compress.working.dir";
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
     * Set to "true" for a personal build, "false" otherwise.
     */
    public static final String PROPERTY_PERSONAL_BUILD          = "personal.build";
    /**
     * The name of the project being built.
     */
    public static final String PROPERTY_PROJECT                 = "project";
    /**
     * The unique 64-bit handle of the project being built.
     */
    public static final String PROPERTY_PROJECT_HANDLE          = "project.handle";
    /**
     * The number of times the project has been built successfully before the
     * running build.
     */
    public static final String PROPERTY_SUCCESS_COUNT           = "success.count";
    /**
     * A short, human-readable summary of the test results for the build
     * (only available post-build).
     */
    public static final String PROPERTY_TEST_SUMMARY            = "test.summary";

    //-----------------------------------------------------------------------
    // Stage/Recipe level properties
    //-----------------------------------------------------------------------

    /**
     * The name of the agent the stage is assigned to.
     */
    public static final String PROPERTY_AGENT                   = "agent";
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
     * An internal property used by agents to locate files needed during the
     * build.
     */
    public static final String PROPERTY_FILE_REPOSITORY         = "file.repository";
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
     * The name of the stage being built.
     */
    public static final String PROPERTY_STAGE                   = "stage";
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
