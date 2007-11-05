package com.zutubi.pulse.core;

import java.text.SimpleDateFormat;

/**
 * Defines the property names for various build properties.
 */
public class BuildProperties
{
    // Build
    public static final String PROPERTY_BUILD_COUNT             = "build.count";
    public static final String PROPERTY_BUILD_NUMBER            = "build.number";
    public static final String PROPERTY_BUILD_REASON            = "build.reason";
    public static final String PROPERTY_BUILD_REVISION          = "build.revision";
    public static final String PROPERTY_BUILD_TIMESTAMP         = "build.timestamp";
    public static final String PROPERTY_BUILD_TIMESTAMP_MILLIS  = "build.timestamp.millis";
    public static final String PROPERTY_BUILD_TRIGGER           = "build.trigger";
    public static final String PROPERTY_CLEAN_BUILD             = "clean.build";
    public static final String PROPERTY_MASTER_URL              = "master.url";
    public static final String PROPERTY_PROJECT                 = "project";
    public static final String PROPERTY_SUCCESS_COUNT           = "success.count";

    // Recipe
    public static final String PROPERTY_BASE_DIR                = "base.dir";
    public static final String PROPERTY_FILE_REPOSITORY         = "file.repository";
    public static final String PROPERTY_RECIPE_ID               = "recipe.id";
    public static final String PROPERTY_RECIPE_PATHS            = "recipe.paths";
    public static final String PROPERTY_RECIPE_TIMESTAMP        = "recipe.timestamp";
    public static final String PROPERTY_RECIPE_TIMESTAMP_MILLIS = "recipe.timestamp.millis";
    public static final String PROPERTY_RESOURCE_REPOSITORY     = "resource.repository";
    public static final String PROPERTY_TEST_RESULTS            = "test.results";

    // Command
    public static final String PROPERTY_OUTPUT_DIR             = "output.dir";

    // Other constants
    public static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

}
